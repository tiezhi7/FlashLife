package com.flashlife.dto;
/*
 * 点赞操作结果。
 */
public class LikeResponse {
    /*
     * 当前用户是否点赞。
     */
    private boolean liked;
    /*
     * 当前点赞总数。
     */
    private long likeCount;
    public LikeResponse() {
    }
    public LikeResponse(
            boolean liked,
            long likeCount
    ) {
        this.liked = liked;
        this.likeCount = likeCount;
    }
    public boolean isLiked() {
        return liked;
    }
    public long getLikeCount() {
        return likeCount;
    }
}
