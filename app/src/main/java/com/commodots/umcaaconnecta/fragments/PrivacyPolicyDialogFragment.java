package com.commodots.umcaaconnecta.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commodots.umcaaconnecta.R;

public class PrivacyPolicyDialogFragment extends BaseFullDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_privacy, container);
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        TextView privacy = view.findViewById(R.id.textprivacy);

        privacy.setText(Html.fromHtml(getString(R.string.privacy_policy_html)));

        return view;
    }
}
