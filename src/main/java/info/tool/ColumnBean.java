package info.tool;

public class ColumnBean {

    /** 項目名 */
    private String name = null;
    /** バイト数 */
    private Integer byte_length = null;
    /** 全角文字指定 */
    private boolean full = false;
    /** ランダム文字配置 */
    private boolean random = false;
    /** 文字内容 */
    private String type = null;
    /** インデックス */
    private boolean idx = false;
    /** インデックス項目開始値 */
    private Integer idx_start = null;
    /** インデックス左半角スペース埋め */
    private boolean idx_space = false;
    /** 固定左文字列 */
    private String lsetlen = null;
    /** 固定右文字列 */
    private String rsetlen = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getByte_length() {
        return byte_length;
    }

    public void setByte_length(Integer byte_length) {
        this.byte_length = byte_length;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIdx() {
        return idx;
    }

    public void setIdx(boolean idx) {
        this.idx = idx;
    }

    public Integer getIdx_start() {
        return idx_start;
    }

    public void setIdx_start(Integer idx_start) {
        this.idx_start = idx_start;
    }

    public boolean isIdx_space() {
        return idx_space;
    }

    public void setIdx_space(boolean idx_space) {
        this.idx_space = idx_space;
    }

    public String getLsetlen() {
        return lsetlen;
    }

    public void setLsetlen(String lsetlen) {
        this.lsetlen = lsetlen;
    }

    public String getRsetlen() {
        return rsetlen;
    }

    public void setRsetlen(String rsetlen) {
        this.rsetlen = rsetlen;
    }

}
