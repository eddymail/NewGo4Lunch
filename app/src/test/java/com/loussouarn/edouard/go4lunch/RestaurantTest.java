package com.loussouarn.edouard.go4lunch;

import com.loussouarn.edouard.go4lunch.model.Restaurant;
import com.loussouarn.edouard.go4lunch.model.User;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RestaurantTest {

    private String nameTest = "Name Test";
    private String emailTest = "email@test.fr";
    private String pictureTest = "Picture Test";

    private Restaurant restaurantForTest = new Restaurant();
    private List<String> clientsTodayList = new ArrayList<>();

    @Test
    public void getClientsTodayList_success(){

        // GIVEN : Create new Users
        User firstUserForTest = new User(nameTest, emailTest, pictureTest);
        User secondUserForTest = new User("name 2","email2@test.fr","Picture2" );

        // WHEN : Add users in clientsTodayList and the list to the restaurant
        clientsTodayList.add(firstUserForTest.getUid());
        clientsTodayList.add(secondUserForTest.getUid());
        restaurantForTest.setClientsTodayList(clientsTodayList);

        // THEN: The list contains 2 users
        Assert.assertEquals(restaurantForTest.getClientsTodayList().size(), 2);

    }
}
