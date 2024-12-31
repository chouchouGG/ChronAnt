package cn.uhoc.domain.task;

import cn.uhoc.domain.launcher.entity.TaskContext;
import cn.uhoc.domain.launcher.entity.TaskStageResult;
import cn.uhoc.domain.register.MultiStageAsyncTask;
import cn.uhoc.domain.register.TaskStage;
import com.alibaba.fastjson.JSON;

// RAG 解题任务
@MultiStageAsyncTask(taskType = "RAG")
public class RAG implements IExecutableAsyncTask {

    /**
     * 第一阶段：数据加载与处理
     */
    @TaskStage(nextStage = "retrieveKnowledge")
    public TaskRet loadData(String input) {
        System.out.println("第一阶段开启: 加载用户查询问题: " + input);
        
        // 模拟加载题库和预处理
        String processedQuery = "处理后的用户问题: " + input;
        return new TaskRet("数据加载完成", processedQuery);
    }

    /**
     * 第二阶段：知识库检索
     */
    @TaskStage(nextStage = "generateAnswer")
    public TaskRet retrieveKnowledge(String processedQuery) {
        System.out.println("第二阶段开启: 根据用户问题检索知识库，问题为: " + processedQuery);
        
        // 模拟检索知识库，返回相关的知识
        String retrievedKnowledge = "检索结果: 相关知识点1, 知识点2, 知识点3";
        return new TaskRet("知识库检索完成", retrievedKnowledge);
    }

    /**
     * 第三阶段：生成答案
     */
    @TaskStage(nextStage = "optimizeAnswer")
    public TaskRet generateAnswer(String retrievedKnowledge) {
        System.out.println("第三阶段开启: 根据检索到的知识生成初步答案: " + retrievedKnowledge);
        
        // 模拟生成模型生成答案
        String generatedAnswer = "生成的答案: 这是一个针对问题的初步回答。";
        return new TaskRet("答案生成完成", generatedAnswer);
    }

    /**
     * 第四阶段：优化答案
     */
    @TaskStage(nextStage = "handleFinish")
    public TaskRet optimizeAnswer(String generatedAnswer) {
        System.out.println("第四阶段开启: 优化生成的答案: " + generatedAnswer);
        
        // 模拟答案优化
        String optimizedAnswer = "优化后的答案: 这是一个针对问题的高质量回答。";
        return new TaskRet("答案优化完成");
    }

    /**
     * 任务入口方法：任务初始化
     */
    @TaskStage(nextStage = "loadData")
    @Override
    public TaskRet handleProcess() {
        System.out.println("任务初始化：开始 RAG 解题任务");
        // 模拟用户输入问题
        String userInput = "用户问题: 如何实现快速检索和生成结合的问答系统？";
        return new TaskRet("任务初始化完成", userInput);
    }

    /**
     * 任务完成后置处理
     */
    @TaskStage
    @Override
    public TaskRet handleFinish() {
        System.out.println("任务后置处理：任务完成后释放资源或执行后续任务");
        return new TaskRet("RAG 解题任务全部阶段执行完毕");
    }

    /**
     * 任务执行出错处理
     */
    @Override
    public TaskStageResult handleError() {
        System.out.println("任务执行出错，转人工检查");
        return new TaskStageResult(null, "任务执行出错，请检查问题或知识库数据");
    }

    /**
     * 任务执行失败处理
     */
    @Override
    public TaskStageResult handleFailure() {
        System.out.println("任务失败，记录失败原因");
        return new TaskStageResult(null, "任务失败，请重新尝试或联系支持团队");
    }

    /**
     * 上下文加载
     */
    @Override
    public TaskContext contextLoad(String context) {
        return JSON.parseObject(context, TaskContext.class);
    }
}
