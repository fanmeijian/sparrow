package cn.sparrowmini.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Setter
@Getter
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    public String code;
    public String message;
    public T data;

    public ApiResponse<T> success(T data) {
        return new ApiResponse<T>("0", "成功", data);
    }


    public ApiResponse(String code, String message, T data) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(T data) {
        super();
        this.code = "0";
        this.message = "success";
        this.data = data;
    }


}
