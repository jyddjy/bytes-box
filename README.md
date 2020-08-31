### 服务扩展模块 

##### support-common-box 服务公共扩展

##### support-data-box 服务数据操作扩展

##### support-event-box 服务事件扩展

##### support-test-box 服务测试扩展

#### support-common-box 实现功能

* 加解密操作

  > 服务之间的通信可以要求加解密，服务端也可以忽略加解密

  * 操作示例

    ```java
    1.nacos 配置公共使用的公钥信息
      
    support:
      box:
        public-keys:
          bfs-demo:
            - profile: local
              publicKey: MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQ7ftAcGVTtZNlgaCl1R9wtbeZxOOCLweX0QknSc/Pvshx4kcodlYf5OzeV0GM8T2SFUuUoko5ggTjTlDaV9wikAdCvPM56ds3/BWmdZ9yxzYhc0216EzzFKKTstchbx9UOG2ePAmVa8/F66GRy0su2G8u29ywtgSEulTyZgOkPwIDAQAB
                
    2.本地配置自己的私钥信息（也可以自己配置其他服务的公钥信息，支持环境隔离）
                
    support:
      box:
        open-ignore: false
        private-key: MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANDt+0BwZVO1k2WBoKXVH3C1t5nE44IvB5fRCSdJz8++yHHiRyh2Vh/k7N5XQYzxPZIVS5SiSjmCBONOUNpX3CKQB0K88znp2zf8FaZ1n3LHNiFzTbXoTPMUopOy1yFvH1Q4bZ48CZVrz8XroZHLSy7Yby7b3LC2BIS6VPJmA6Q/AgMBAAECgYARJaGb8Eg1+TkCTlk47aeJleBeUQ0QVNEdYfKLtEBx9F55ukXMyamChJn2xYKDvBN+4cXfilI9K1vrVoocNrRs8RkouXK88uH+caTGzEKgAmXH6157vIzWyQzOIhttkn9SQ8dzc2pPhySdzmMv+LLBmzPg1vRnmsJTImiUacLugQJBAO9qRubAK0W3RSlUZmfiC9GlXYszuS0FBX0++X0bBAdmia1oCbVEE039SJkj6o0jY0kY5KVxx+DPIMYQIIMWUWkCQQDfZxUYy5DZPmlyxq/SZmKIIz+6GusScsutTW4X1YOs6jDSJJXpv+kbd6HcBq3pXoPyVBEXEoOW7ORMz1jRsmtnAkEAmAZlNTtLfv5sJV1ZlZd9J8eYyb6zVDn/DVPCusU/3q8mt3z7xSWQGJK/2bZrxul0r0LmVYQVhtQfnIU7D+JJGQJAb/qpln4jIuGn1YJGCz6K0RZqEQ0BG4QzF7EiKAw8LmZNBmiURFCPtdbnaPRoI/veQ49j6Z68GuCvJtkf+ixmrQJBAKUwvDi+JJn6IYE4JM7hBBnvueA4yZBb4Kh1UGFtuSr1/O7KrAW8cBK9S67ZcaemrL7DPGV4uSTcCj4A3xge5Zo=
        public-keys:
          bfs-demo2:
            - profile: local
              publicKey: MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQ7ftAcGVTtZNlgaCl1R9wtbeZxOOCLweX0QknSc/Pvshx4kcodlYf5OzeV0GM8T2SFUuUoko5ggTjTlDaV9wikAdCvPM56ds3/BWmdZ9yxzYhc0216EzzFKKTstchbx9UOG2ePAmVa8/F66GRy0su2G8u29ywtgSEulTyZgOkPwIDAQAB
                
    3.服务开启密钥设置
    @EnableBox(encrypt = true)
    public class MainApplication {
        public static void main(String[] args) {
            SpringApplication.run(MainApplication.class, args);
        }
    }
    4.调用端（web）添加header,用于标识自己的身份信息，一般用注册名称加上自己当前启动的环境
      __encrypt__ctx__: 
    {"source":"bfs-demo2","profile":"local","reqIgnore":true,"respIgnore":false}
   
    5.具体的可以看demo2的操作
    
    ```

    



