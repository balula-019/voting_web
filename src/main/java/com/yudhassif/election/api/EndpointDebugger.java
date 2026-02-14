//package com.yudhassif.election.api;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
//
//@Component
//@RequiredArgsConstructor
//public class EndpointDebugger {
//
//    private final RequestMappingHandlerMapping handlerMapping;
//
//    @PostConstruct
//    public void printEndpoints() {
//        handlerMapping.getHandlerMethods().forEach((key, value) -> {
//            System.out.println(
//                    key + " ==> " + value.getMethod().getName()
//            );
//        });
//    }
//}
//
