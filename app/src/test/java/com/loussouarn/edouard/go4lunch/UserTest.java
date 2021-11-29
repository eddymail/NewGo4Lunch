package com.loussouarn.edouard.go4lunch;

import com.loussouarn.edouard.go4lunch.model.Restaurant;
import com.loussouarn.edouard.go4lunch.model.User;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UserTest {

    private String nameTest = "Name Test";
    private String emailTest = "email@test.fr";
    private String pictureTest = "Picture Test";
    private String sushiShop = "Sushi Shop";

    @Test
    public void setRestaurantOfTheDayName_Success()
    {
        // GIVEN : Create a new User
        User userForTest = new User(emailTest, nameTest, pictureTest);

        // WHEN : Add the chosen restaurant
        userForTest.setRestaurantOfTheDayName(sushiShop);

        // THEN : Verify boolean and restaurantChoose changed
        assertEquals(userForTest.getRestaurantOfTheDayName(), sushiShop);

    }


}
