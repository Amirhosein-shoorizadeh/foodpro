package dto;

import java.util.List;

public class FoodDto {

    public long id;

    public String name;

    public String imageBase64;

    public String description;

    public long vendor_id;

    public long price;

    public int  supply;

    public List<String> keywords;

    public FoodDto(){}
    public FoodDto(long id,String name, String imageBase64, String description,long vendor_id ,Integer price, Integer supply, List<String> keywords) {
        this.id = id;
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.vendor_id = vendor_id;
        this.price = price;
        this.supply = supply;
        this.keywords = keywords;
    }


}
