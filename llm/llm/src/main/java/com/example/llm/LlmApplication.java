package com.example.llm; 

import java.time.Duration;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.example.llm.LlmApplication.AdvancedCalculator;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;


interface Chatbot {

	@SystemMessage("""
		你是一个计算机专家""")
    String chat(@MemoryId String sessionID, @UserMessage String question);
} 
 

public class LlmApplication {

    static class AdvancedCalculator {
		/*大模型通过@Tool注释“理解”工具功能，注释的“人类可读性”直接决定模型的判断精度
		  模糊的注释（如“处理数字”）会让模型困惑，
   	      具体的注释（如“执行加减乘除运算，输入格式为‘操作数 运算符 操作数’”）能让模型明确调用场景*/
        @Tool("解一元二次方程ax²+bx+c=0,返回两个根")
public double[] solveQuadratic(double a, double b, double c) {
    double deltaSqrt = Math.sqrt(b * b - 4 * a * c);
    double x1 = (-b+deltaSqrt ) / (2*a);
    double x2 = (-b-deltaSqrt ) / (2*a);
    return new double[]{x1, x2};
}

    }
    public static void main(String[] args) {
        // Replace with your actual Zhipu AI API key
        String apiKey = "00af8b4a4d2848809bca27d335f2c738.3oKxp1mPWmUJb3e8";
        
        // Create a Zhipu AI chat model
        ChatLanguageModel model = ZhipuAiChatModel.builder()
                .apiKey(apiKey)
                .model("glm-4-flash") // You can use "glm-3-turbo" or other available models
                .temperature(0.1)
                .callTimeout(Duration.ofSeconds(60))
                .connectTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .maxToken(500)
                .build();

        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);
        AdvancedCalculator calculator = new AdvancedCalculator();

        Chatbot mathTutor = AiServices.builder(Chatbot.class)
            .chatLanguageModel(model)
            .tools(calculator)
            .chatMemoryProvider(sessionID -> memory)	
            .build(); 

        // Interactive chat example
        System.out.println("\nStarting interactive chat (type 'exit' to quit)");
        interactiveChat(mathTutor);
    }

    private static void interactiveChat(Chatbot model) {
        String sessionID = "student-123";
        try (Scanner scanner = new Scanner(System.in)) {
            String userInput;
            
            while (true) {
                System.out.print("You: ");
                userInput = scanner.nextLine();
                
                if ("exit".equalsIgnoreCase(userInput.trim())) {
                    break;
                }
                
                String response = model.chat(sessionID, userInput);
                System.out.println("AI: " + response + "\n");

            }
        }
        System.out.println("Chat ended.");
    }
}
