package cn.sparrowmini.owl.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OwlClassTree {
    public String code;
    public String label;
    public List<OwlClassTree> children = new ArrayList<>();
}