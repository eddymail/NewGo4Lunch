package com.loussouarn.edouard.go4lunch;

import com.loussouarn.edouard.go4lunch.model.Restaurant;
import com.loussouarn.edouard.go4lunch.model.User;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserTest {

    private String nameTest = "Name Test";
    private String emailTest = "email@test.fr";
    private String pictureTest = "Picture Test";
    private String sushiShop = "Sushi Shop";
    private Restaurant restaurantForTest = new Restaurant();


    @Test
    public void setRestaurantOfTheDayName_Success() {
        // GIVEN : Create a new User
        User userForTest = new User(emailTest, nameTest, pictureTest);

        // WHEN : Add the chosen restaurant
        userForTest.setRestaurantOfTheDayName(sushiShop);

        // THEN : Verify boolean and restaurantChoose changed
        assertEquals(userForTest.getRestaurantOfTheDayName(), sushiShop);
    }
}
