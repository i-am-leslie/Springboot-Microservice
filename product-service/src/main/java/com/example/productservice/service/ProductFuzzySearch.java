package com.example.productservice.service;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductFuzzySearch {
    private ProductRepository productRepo;

    public ProductFuzzySearch(ProductRepository productRepo){
        this.productRepo=productRepo;
    }

    /**
     *
     * @param str1
     * @param str2
     * @return
     *
     */
    private int levenshteinDistance(String str1, String str2){
        int minimumDistance;
        int sizeString1= str1.length();
        int sizeString2= str2.length();

        System.out.println(str1 + " " + str2);

        int[][] dp= new int[sizeString1 + 1][sizeString2 +1];

        for(int i=0; i < sizeString1 + 1; i++ ) dp[i][0]=i;  // fill up the indexes with i
        for(int j=0; j < sizeString2 + 1; j++ ) dp[0][j]=j; // fill up the indexes with j

        for (int i = 1; i <= sizeString1; i++) {
            for (int j = 1; j <= sizeString2; j++) {
                // If characters match, no cost to align them
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    // If not equal, take the minimum of:
                    // - Insert (dp[i][j-1])
                    // - Delete (dp[i-1][j])
                    // - Replace (dp[i-1][j-1])
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j], // delete
                            Math.min(
                                    dp[i][j - 1], // insert
                                    dp[i - 1][j - 1] // replace
                            )
                    );
                }
            }
        }
        minimumDistance=dp[sizeString1][sizeString2];
        return minimumDistance;
    }

    public Product searchProducts(String usersProductName){

        Product p=null; // dummy
        int min=Integer.MAX_VALUE;
        int levDistance;

        Iterable<Product> productList=  productRepo.findAll();

        for(Product product :  productList){
            levDistance= levenshteinDistance(usersProductName, product.getName());
            System.out.println("Currently comparing this product:"+" " + usersProductName + " "+"and"+ " " + product.getName());
            if(levDistance < min ){
                p=product;
                min=levDistance;
            }
        }
        return p;
    }



}
