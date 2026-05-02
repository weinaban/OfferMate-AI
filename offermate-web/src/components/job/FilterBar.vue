<template>
  <section class="filter-bar">
    <div class="filter-section">
      <h3>城市</h3>
      <div class="option-list">
        <button
          v-for="city in cities"
          :key="city.value"
          :class="{ active: modelValue.city === city.value }"
          type="button"
          @click="updateField('city', city.value)"
        >
          {{ city.label }}
        </button>
      </div>
    </div>

    <div class="filter-section">
      <h3>薪资</h3>
      <div class="option-list">
        <button
          v-for="salary in salaries"
          :key="salary.key"
          :class="{ active: modelValue.salaryKey === salary.key }"
          type="button"
          @click="updateSalary(salary)"
        >
          {{ salary.label }}
        </button>
      </div>
    </div>

    <div class="filter-section">
      <h3>学历</h3>
      <div class="option-list">
        <button
          v-for="education in educations"
          :key="education.value"
          :class="{ active: modelValue.education === education.value }"
          type="button"
          @click="updateField('education', education.value)"
        >
          {{ education.label }}
        </button>
      </div>
    </div>

    <div class="filter-section">
      <h3>经验</h3>
      <div class="option-list">
        <button
          v-for="experience in experiences"
          :key="experience.value"
          :class="{ active: modelValue.experience === experience.value }"
          type="button"
          @click="updateField('experience', experience.value)"
        >
          {{ experience.label }}
        </button>
      </div>
    </div>

    <div class="filter-section">
      <h3>行业</h3>
      <div class="option-list">
        <button
          v-for="industry in industries"
          :key="industry.value"
          :class="{ active: modelValue.industry === industry.value }"
          type="button"
          @click="updateField('industry', industry.value)"
        >
          {{ industry.label }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const cities = [
  { label: '不限', value: '' },
  { label: '北京', value: '北京' },
  { label: '上海', value: '上海' },
  { label: '深圳', value: '深圳' },
  { label: '广州', value: '广州' },
  { label: '杭州', value: '杭州' },
  { label: '成都', value: '成都' },
  { label: '武汉', value: '武汉' },
  { label: '南京', value: '南京' },
  { label: '苏州', value: '苏州' }
]

const salaries = [
  { label: '不限', key: '', salaryMin: '', salaryMax: '' },
  { label: '3K以下', key: '0-3', salaryMin: '', salaryMax: 3 },
  { label: '3-5K', key: '3-5', salaryMin: 3, salaryMax: 5 },
  { label: '5-10K', key: '5-10', salaryMin: 5, salaryMax: 10 },
  { label: '10-20K', key: '10-20', salaryMin: 10, salaryMax: 20 },
  { label: '20K以上', key: '20+', salaryMin: 20, salaryMax: '' }
]

const educations = [
  { label: '不限', value: '' },
  { label: '大专', value: '大专' },
  { label: '本科', value: '本科' },
  { label: '硕士', value: '硕士' },
  { label: '博士', value: '博士' }
]

const experiences = [
  { label: '不限', value: '' },
  { label: '应届', value: '应届' },
  { label: '1年内', value: '1年内' },
  { label: '1-3年', value: '1-3年' },
  { label: '3-5年', value: '3-5年' },
  { label: '5年以上', value: '5年以上' }
]

const industries = [
  { label: '不限', value: '' },
  { label: '互联网', value: '互联网' },
  { label: '人工智能', value: '人工智能' },
  { label: '软件开发', value: '软件开发' },
  { label: '电子商务', value: '电子商务' },
  { label: '金融', value: '金融' },
  { label: '教育', value: '教育' },
  { label: '制造业', value: '制造业' },
  { label: '医疗健康', value: '医疗健康' }
]

function updateField(field, value) {
  emit('update:modelValue', {
    ...props.modelValue,
    [field]: value
  })
}

function updateSalary(salary) {
  emit('update:modelValue', {
    ...props.modelValue,
    salaryKey: salary.key,
    salaryMin: salary.salaryMin,
    salaryMax: salary.salaryMax
  })
}
</script>

<style scoped>
.filter-bar {
  display: grid;
  gap: 18px;
  padding: 22px 24px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.filter-section {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.filter-section + .filter-section {
  padding-top: 18px;
  border-top: 1px solid #f0f2f5;
}

h3 {
  margin: 6px 0 0;
  color: #111827;
  font-size: 15px;
}

.option-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

button {
  min-width: 62px;
  height: 32px;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 4px;
  color: #4b5563;
  background: #f6f7f9;
  cursor: pointer;
  transition: all 0.2s ease;
}

button:hover,
button.active {
  color: #00a7a6;
  border-color: rgba(0, 190, 189, 0.28);
  background: #e9fbfb;
}
</style>
