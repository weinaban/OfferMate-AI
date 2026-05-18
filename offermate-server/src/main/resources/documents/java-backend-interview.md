# Java 后端面试题库（示例）

> 用作 OfferMate 模拟面试 RAG 知识库样例。可按目录新增 .md / .pdf / .docx 文件，启动时会自动入库。

## Java 基础

- HashMap 的底层数据结构？JDK 1.7 与 1.8 区别？什么时候链表转红黑树（阈值 8）、什么时候退化（阈值 6）？
- ConcurrentHashMap 为什么比 HashTable 快？分段锁 vs CAS+synchronized 的演进。
- volatile 解决了什么问题？保证可见性和禁止指令重排序，但不保证原子性。
- synchronized 与 ReentrantLock 区别？公平锁、可中断、Condition 等。
- 线程池核心参数有哪些？拒绝策略有哪几种？为什么 Executors 不推荐用 newFixedThreadPool？

## JVM

- JVM 内存结构：堆、栈、方法区、程序计数器、本地方法栈。
- 垃圾回收算法：标记-清除、复制、标记-整理；分代收集理论。
- G1 与 CMS 的区别？什么场景下用 ZGC？
- 类加载过程：加载、验证、准备、解析、初始化；双亲委派模型为什么不可破坏。
- 一次 Full GC 排查思路：jstat / jmap / MAT / gc 日志。

## Spring / Spring Boot

- IoC 和 AOP 的本质是什么？BeanFactory 与 ApplicationContext 的区别。
- 循环依赖怎么解决？三级缓存的作用，为什么构造器循环依赖无法解决？
- @Transactional 失效的场景：方法非 public、自调用、异常类型不匹配、传播行为不当。
- Spring Boot 自动装配原理：@EnableAutoConfiguration、spring.factories / AutoConfiguration.imports。
- 怎么自定义一个 starter？需要哪些关键文件？

## MySQL

- 索引为什么用 B+ 树而不是 B 树或哈希？
- 聚簇索引和非聚簇索引的区别？回表是什么？
- 事务的 ACID 性质，四种隔离级别和分别解决了什么问题。
- MVCC 是怎么实现的？undo log + ReadView 的协作。
- 慢 SQL 排查：EXPLAIN 的关键字段（type、key、rows、Extra）。

## Redis

- Redis 为什么快？单线程 + 内存 + IO 多路复用。
- 缓存穿透、缓存击穿、缓存雪崩的区别和应对方案。
- Redis 持久化：RDB 和 AOF 的优劣，混合持久化是什么。
- 分布式锁如何用 Redis 实现？Redisson 的看门狗机制。
- Redis 集群模式：主从、哨兵、Cluster 各自适用场景。

## 消息队列

- RabbitMQ vs Kafka vs RocketMQ 的核心差异。
- 如何保证消息不丢失？生产端确认、Broker 持久化、消费端 ACK。
- 重复消费如何处理？幂等性设计（业务唯一键 + 去重表 / Redis SETNX）。
- 消息积压怎么排查和处理？

## 项目经验追问

- 你这个项目 QPS 大概多少？数据库表多大？瓶颈在哪？
- 简历里写了用 Redis 做缓存，缓存键设计是什么？过期策略是什么？
- 如果让你重新设计这个模块，你会怎么改进？
- 你在项目中遇到的最难的一个 bug 是什么？怎么排查的？
