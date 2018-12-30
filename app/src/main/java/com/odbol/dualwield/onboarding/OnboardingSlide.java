package com.odbol.dualwield.onboarding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.odbol.respects.R;

import java.util.ArrayList;
import java.util.List;

public class OnboardingSlide extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private int layoutResId;

    private int label;
    private int[] images;
    private String description;
    private TextView labelView;
    private TextView descriptionView;


    private int slideIndex;

    private final List<ImageView> imageViews = new ArrayList<>(3);

    public List<ImageView> getImageViews() {
        return imageViews;
    }

    public void setSlideIndex(int slideIndex) {
        this.slideIndex = slideIndex;
    }

    public static OnboardingSlide newInstance(int label, int... images) {
        OnboardingSlide sampleSlide = new OnboardingSlide();

        sampleSlide.layoutResId = R.layout.onboarding_slide;
        sampleSlide.label = label;
        sampleSlide.images = images;

        return sampleSlide;
    }

    public static OnboardingSlide newInstance(String description, int label, int... images) {
        OnboardingSlide sampleSlide = newInstance(label, images);

        sampleSlide.description = description;

        return sampleSlide;
    }

    public static OnboardingSlide newInstance(int layoutResId) {
        OnboardingSlide sampleSlide = new OnboardingSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, container, false);

        labelView = (TextView) view.findViewById(R.id.label);
        descriptionView = (TextView) view.findViewById(R.id.description);
        ImageView[] imageViewHolders = new ImageView[3];
        imageViewHolders[0] = (ImageView) view.findViewById(R.id.image_right);
        imageViewHolders[1] = (ImageView) view.findViewById(R.id.image);
        imageViewHolders[2] = (ImageView) view.findViewById(R.id.image_left);

        labelView.setText(label);

        if (!TextUtils.isEmpty(description)) {
            descriptionView.setText(description);
        }

        if (images.length == 1) {
            imageViewHolders[1].setImageResource(images[0]);
            imageViews.add(imageViewHolders[1]);
        } else {
            for (int i = 0; i < images.length; i++) {
                int image = images[i];
                imageViewHolders[i].setImageResource(image);
                imageViews.add(imageViewHolders[i]);
            }
        }

        view.setTag(slideIndex);


        return view;
    }



}