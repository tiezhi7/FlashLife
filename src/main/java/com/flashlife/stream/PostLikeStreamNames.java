package com.flashlife.stream;
/*
 * PostLikeStreamNames
 * 专门保存点赞 Stream 使用的固定名称。
 * 好处：Producer 和 Consumer
 * 使用同一份常量，避免：
 * 一个地方写错字符
 * 导致生产者和消费者连接到两个不同 Stream。
 */
public final class PostLikeStreamNames {
    /*
     * Redis Stream Key。
     */
    public static final String STREAM_KEY =
            "flashlife:stream:post-like-events";
    /*
     * Consumer Group 名称。
     */
    public static final String GROUP_NAME =
            "flashlife-post-like-persist-group";
    /*
     * 工具类不允许创建实例。
     */
    private PostLikeStreamNames() {
    }
}
