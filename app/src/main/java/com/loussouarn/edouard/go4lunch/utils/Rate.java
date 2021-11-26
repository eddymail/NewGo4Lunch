package com.loussouarn.edouard.go4lunch.utils;

import android.view.View;
import android.widget.ImageView;

public class Rate {

    // No star under 2.5, 1 star between 2.6 and 3.5, 2 stars between 3.6 and 4.5, and 3 stars above
    public Rate(double rate, ImageView star1, ImageView star2, ImageView star3) {
        int rate_int = (int) Math.round(rate);

        switch (rate_int) {
            case 0:
            case 1:
            case 2:
                star1.setVisibility(View.GONE);
                star2.setVisibility(View.GONE);
                star3.setVisibility(View.GONE);
                break;
            case 3:
                star1.setVisibility(View.GONE);
                star2.setVisibility(View.GONE);
                star3.setVisibility(View.VISIBLE);
                break;
            case 4:
                star1.setVisibility(View.GONE);
                star2.setVisibility(View.VISIBLE);
                star3.setVisibility(View.VISIBLE);
                break;
            case 5:
                star1.setVisibility(View.VISIBLE);
                star2.setVisibility(View.VISIBLE);
                star3.setVisibility(View.VISIBLE);
                break;
        }
    }
}
