Spring Boot 默认的请求处理模型是：一个请求进入 Tomcat 后，会从 Tomcat 线程池中获取一个工作线程，
然后 Controller、Service、DAO 等代码都会在这个线程中同步执行。只有使用异步机制（@Async、线程池、MQ、并行流等）时，才会产生新的线程。

这个概念是后面理解 Spring事务、ThreadLocal、@Async失效、链路追踪、分布式事务 的基础。你现在学习 ThreadLocal，刚好要先把这个线程模型搞清楚。