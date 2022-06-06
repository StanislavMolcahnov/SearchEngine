package main.dto;

import java.util.List;

public class SearchResultDto {
    public boolean result;
    public int count;

    public List<SearchPageDto> data;
    public String error;
}
