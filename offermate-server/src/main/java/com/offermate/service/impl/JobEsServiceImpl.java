package com.offermate.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.offermate.common.result.PageResult;
import com.offermate.dto.JobSearchDTO;
import com.offermate.entity.Company;
import com.offermate.entity.JobPosition;
import com.offermate.es.JobEsDocument;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.CompanyMapper;
import com.offermate.mapper.JobPositionMapper;
import com.offermate.service.JobEsService;
import com.offermate.util.PageUtils;
import com.offermate.vo.JobPageVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobEsServiceImpl implements JobEsService {

    private static final String INDEX_NAME = "offermate_job";
    private static final int ONLINE_STATUS = 1;
    private static final int AUDIT_PASS = 1;
    private static final String SORT_LATEST = "latest";
    private static final String SORT_SALARY = "salary";

    private final ElasticsearchClient elasticsearchClient;
    private final JobPositionMapper jobPositionMapper;
    private final CompanyMapper companyMapper;

    @PostConstruct
    public void initIndex() {
        try {
            createIndexIfNotExists();
        } catch (Exception e) {
            log.warn("ES岗位索引初始化失败", e);
        }
    }

    @Override
    public void createIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(INDEX_NAME))
                    .value();
            if (exists) {
                return;
            }

            elasticsearchClient.indices().create(c -> c
                    .index(INDEX_NAME)
                    .mappings(m -> m
                            .properties("id", p -> p.long_(v -> v))
                            .properties("companyId", p -> p.long_(v -> v))
                            .properties("recruiterId", p -> p.long_(v -> v))
                            // 当前使用 standard analyzer；后续安装 IK 后可改为 ik_max_word。
                            .properties("title", p -> p.text(v -> v.analyzer("standard")))
                            .properties("companyName", p -> p.text(v -> v.analyzer("standard")))
                            .properties("tags", p -> p.text(v -> v.analyzer("standard")))
                            .properties("description", p -> p.text(v -> v.analyzer("standard")))
                            .properties("companyLogo", p -> p.keyword(v -> v))
                            .properties("city", p -> p.keyword(v -> v))
                            .properties("education", p -> p.keyword(v -> v))
                            .properties("experience", p -> p.keyword(v -> v))
                            .properties("industry", p -> p.keyword(v -> v))
                            .properties("scale", p -> p.keyword(v -> v))
                            .properties("status", p -> p.integer(v -> v))
                            .properties("auditStatus", p -> p.integer(v -> v))
                            .properties("salaryMin", p -> p.integer(v -> v))
                            .properties("salaryMax", p -> p.integer(v -> v))
                            .properties("viewCount", p -> p.integer(v -> v))
                            .properties("createTime", p -> p.date(v -> v))
                            .properties("updateTime", p -> p.date(v -> v))));
            log.info("ES岗位索引创建成功 index={}", INDEX_NAME);
        } catch (Exception e) {
            throw new BusinessException("搜索服务暂时不可用，请稍后再试");
        }
    }

    @Override
    public void saveOrUpdateJob(Long jobId) {
        try {
            JobPosition job = jobPositionMapper.selectById(jobId);
            if (job == null || !Integer.valueOf(ONLINE_STATUS).equals(job.getStatus())
                    || !Integer.valueOf(AUDIT_PASS).equals(job.getAuditStatus())) {
                deleteJob(jobId);
                return;
            }

            Company company = companyMapper.selectById(job.getCompanyId());
            JobEsDocument document = toDocument(job, company);
            elasticsearchClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(jobId))
                    .document(document));
        } catch (Exception e) {
            log.warn("ES岗位同步失败 jobId={}", jobId, e);
        }
    }

    @Override
    public void deleteJob(Long jobId) {
        try {
            elasticsearchClient.delete(d -> d.index(INDEX_NAME).id(String.valueOf(jobId)));
        } catch (ElasticsearchException e) {
            if (e.status() != 404) {
                log.warn("ES岗位删除失败 jobId={}", jobId, e);
            }
        } catch (Exception e) {
            log.warn("ES岗位删除失败 jobId={}", jobId, e);
        }
    }

    @Override
    public int syncAll() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(INDEX_NAME)).value();
            if (exists) {
                elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME));
            }
            createIndexIfNotExists();

            List<JobPosition> jobs = jobPositionMapper.selectList(new LambdaQueryWrapper<JobPosition>()
                    .eq(JobPosition::getStatus, ONLINE_STATUS)
                    .eq(JobPosition::getAuditStatus, AUDIT_PASS));
            if (jobs.isEmpty()) {
                return 0;
            }

            Map<Long, Company> companyMap = companyMapper.selectBatchIds(jobs.stream()
                            .map(JobPosition::getCompanyId)
                            .distinct()
                            .toList())
                    .stream()
                    .collect(Collectors.toMap(Company::getId, Function.identity(), (a, b) -> a));

            BulkRequest.Builder builder = new BulkRequest.Builder();
            for (JobPosition job : jobs) {
                builder.operations(op -> op.index(i -> i
                        .index(INDEX_NAME)
                        .id(String.valueOf(job.getId()))
                        .document(toDocument(job, companyMap.get(job.getCompanyId())))));
            }
            elasticsearchClient.bulk(builder.build());
            return jobs.size();
        } catch (Exception e) {
            log.warn("ES岗位全量同步失败", e);
            throw new BusinessException("搜索服务暂时不可用，请稍后再试");
        }
    }

    @Override
    public PageResult<JobPageVO> searchJobs(JobSearchDTO dto) {
        if (dto == null) {
            dto = new JobSearchDTO();
        }
        JobSearchDTO queryDTO = dto;
        int page = PageUtils.page(queryDTO.getPage());
        int pageSize = PageUtils.pageSize(queryDTO.getPageSize());

        try {
            List<Query> filters = new ArrayList<>();
            filters.add(Query.of(q -> q.term(t -> t.field("status").value(FieldValue.of(ONLINE_STATUS)))));
            filters.add(Query.of(q -> q.term(t -> t.field("auditStatus").value(FieldValue.of(AUDIT_PASS)))));
            addTermFilter(filters, "city", queryDTO.getCity());
            addTermFilter(filters, "education", queryDTO.getEducation());
            addTermFilter(filters, "experience", queryDTO.getExperience());
            addTermFilter(filters, "industry", queryDTO.getIndustry());
            if (queryDTO.getSalaryMin() != null) {
                double salaryMin = queryDTO.getSalaryMin().doubleValue();
                filters.add(Query.of(q -> q.range(r -> r.number(n -> n.field("salaryMax").gte(salaryMin)))));
            }
            if (queryDTO.getSalaryMax() != null) {
                double salaryMax = queryDTO.getSalaryMax().doubleValue();
                filters.add(Query.of(q -> q.range(r -> r.number(n -> n.field("salaryMin").lte(salaryMax)))));
            }

            Query keywordQuery = buildKeywordQuery(queryDTO.getKeyword());
            String sort = PageUtils.jobSearchSort(queryDTO.getSort());
            int from = (page - 1) * pageSize;

            SearchResponse<JobEsDocument> response = elasticsearchClient.search(s -> {
                s.index(INDEX_NAME)
                        .from(from)
                        .size(pageSize)
                        .query(q -> q.bool(b -> {
                            b.filter(filters);
                            if (keywordQuery != null) {
                                b.must(keywordQuery);
                            }
                            return b;
                        }));
                if (SORT_LATEST.equals(sort)) {
                    s.sort(v -> v.field(f -> f.field("createTime").order(SortOrder.Desc)));
                } else if (SORT_SALARY.equals(sort)) {
                    s.sort(v -> v.field(f -> f.field("salaryMax").order(SortOrder.Desc)));
                }
                return s;
            }, JobEsDocument.class);

            List<JobPageVO> records = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(document -> document != null)
                    .map(this::toPageVO)
                    .toList();
            long total = response.hits().total() == null ? records.size() : response.hits().total().value();
            return new PageResult<>(total, records);
        } catch (Exception e) {
            log.warn("ES岗位搜索失败", e);
            throw new BusinessException("搜索服务暂时不可用，请稍后再试");
        }
    }

    private Query buildKeywordQuery(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return Query.of(q -> q.multiMatch(m -> m
                .query(keyword)
                .fields("title^3", "companyName^2", "tags^2", "description")));
    }

    private void addTermFilter(List<Query> filters, String field, String value) {
        if (StringUtils.hasText(value)) {
            filters.add(Query.of(q -> q.term(t -> t.field(field).value(value))));
        }
    }

    private JobEsDocument toDocument(JobPosition job, Company company) {
        JobEsDocument document = new JobEsDocument();
        document.setId(job.getId());
        document.setCompanyId(job.getCompanyId());
        document.setRecruiterId(job.getRecruiterId());
        document.setTitle(job.getTitle());
        document.setSalaryMin(job.getSalaryMin());
        document.setSalaryMax(job.getSalaryMax());
        document.setCity(job.getCity());
        document.setExperience(job.getExperience());
        document.setEducation(job.getEducation());
        document.setTags(job.getTags());
        document.setDescription(job.getDescription());
        document.setStatus(job.getStatus());
        document.setAuditStatus(job.getAuditStatus());
        document.setViewCount(job.getViewCount());
        document.setCreateTime(job.getCreateTime());
        document.setUpdateTime(job.getUpdateTime());
        if (company != null) {
            document.setCompanyName(company.getCompanyName());
            document.setCompanyLogo(company.getLogo());
            document.setIndustry(company.getIndustry());
            document.setScale(company.getScale());
        }
        return document;
    }

    private JobPageVO toPageVO(JobEsDocument document) {
        JobPageVO vo = new JobPageVO();
        vo.setId(document.getId());
        vo.setCompanyId(document.getCompanyId());
        vo.setTitle(document.getTitle());
        vo.setSalaryMin(document.getSalaryMin());
        vo.setSalaryMax(document.getSalaryMax());
        vo.setCity(document.getCity());
        vo.setExperience(document.getExperience());
        vo.setEducation(document.getEducation());
        vo.setTags(document.getTags());
        vo.setStatus(document.getStatus());
        vo.setAuditStatus(document.getAuditStatus());
        vo.setViewCount(document.getViewCount());
        vo.setCreateTime(document.getCreateTime());
        vo.setCompanyName(document.getCompanyName());
        vo.setCompanyLogo(document.getCompanyLogo());
        vo.setIndustry(document.getIndustry());
        vo.setScale(document.getScale());
        return vo;
    }
}
