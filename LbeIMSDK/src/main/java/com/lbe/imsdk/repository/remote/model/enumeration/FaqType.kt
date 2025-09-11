package com.lbe.imsdk.repository.remote.model.enumeration

/**
 *
 * @Author mocaris
 * @Date 2025-09-05
 */
enum class FaqType(value: Int) {
    //0-获取知识库
    KNOWLEDGE_BASE(0),

    // 1-获取知识点
    KNOWLEDGE_POINT(1),

    // 2-获取答案
    KNOWLEDGE_ANSWER(2);

    val value: Int = value
}