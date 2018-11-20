package com.futurice.android.reservator.view.wizard;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.PreferenceManager;
import com.github.paolorotolo.appintro.ISlidePolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public final class WizardBackendUrlInput
        extends android.support.v4.app.Fragment implements ISlidePolicy {

    @BindView(R.id.wizard_backend_base_url_title)
    TextView baseUrlTitle;
    @BindView(R.id.wizard_backend_base_url)
    EditText baseUrlInput;
    @BindView(R.id.wizard_backend_sub_url_title)
    TextView subUrlTitle;
    @BindView(R.id.wizard_backend_sub_url)
    EditText subUrlInput;

    Unbinder unbinder;

    AlertDialog alertDialog;
    PreferenceManager preferences;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.wizard_backend_url_input, container, false);
        unbinder = ButterKnife.bind(this, view);

        baseUrlTitle.setText("Input base URL");
        subUrlTitle.setText("Input sub URL");

        preferences = PreferenceManager.getInstance(getActivity());
        baseUrlInput.setText((CharSequence) preferences.getBaseUrl(), TextView.BufferType.EDITABLE);
        String baseUrl = preferences.getBaseUrl();
        subUrlInput.setText(preferences.getSubUrl(), TextView.BufferType.EDITABLE);

        baseUrlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                preferences.setBaseUrl(c.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        subUrlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                preferences.setSubUrl(c.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean isPolicyRespected() {
        return baseUrlInput.length() > 0;
    }

    public void onUserIllegallyRequestedNextPage() {
    }

    public void onPause() {
        super.onPause();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
