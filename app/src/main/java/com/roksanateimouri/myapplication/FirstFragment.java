package com.roksanateimouri.myapplication;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.BLOB_STORE_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.roksanateimouri.myapplication.util.Constant.BASE_URL;
import static com.roksanateimouri.myapplication.util.Constant.BLOCK_IMAGE_KEY;
import static com.roksanateimouri.myapplication.util.Constant.HOME_URL;

public class FirstFragment extends Fragment {
    private WebView webView;
    private EditText etUrl;
    private SharedPreferences preferences;
    private ArrayAdapter<String> arrayAdapter;
    private ProgressBar progressBar;
    private Set<String> websites = new HashSet<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        Toolbar mToolbarContact = view.findViewById(R.id.toolbar);
        webView = view.findViewById(R.id.vw_web);
        etUrl = view.findViewById(R.id.et_url);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbarContact);
        preferences = getActivity().getSharedPreferences(BLOB_STORE_SERVICE, MODE_PRIVATE);
        progressBar = new ProgressBar(getActivity());
        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.view_top_list_item, R.id.tv_item);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                showSettingDialog();
                break;
            case R.id.action_Home:
                home();
                break;
            case R.id.action_refresh:
                loading();
                break;
            case R.id.action_top_list:
                showTopListDialog();
                break;
            default:
                break;
        }
        return false;
    }

    void showTopListDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireActivity(), R.style.TopTenDialogTheme);
        builderSingle.setTitle(Html.fromHtml("<b>" + getString(R.string.alert_dialog_top_list_text) + "</b>"));
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                webView.loadUrl(strName);
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }

    private void loading() {
        progressBar.setVisibility(View.VISIBLE);
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webView.setWebViewClient(new MyBrowser());
        webSetting.setJavaScriptEnabled(true);
    }

    public void home() {
        loading();
        webView.loadUrl(HOME_URL);
    }

    private void showSettingDialog() {
        new AlertDialog.Builder(requireActivity(), R.style.SettingDialogTheme)
                .setMessage(R.string.alert_dialog_setting_text)
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(R.string.alert_dialog_setting_btn_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        preferences.edit().putBoolean(BLOCK_IMAGE_KEY, true).apply();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_setting_btn_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        preferences.edit().putBoolean(BLOCK_IMAGE_KEY, false).apply();
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnForward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading();
                webView.loadUrl(BASE_URL + etUrl.getText().toString());
            }
        });
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            websites.add(url);
            arrayAdapter = new ArrayAdapter<>(requireContext(), R.layout.view_top_list_item,R.id.tv_item, new ArrayList<>(websites));
            if (preferences.getBoolean(BLOCK_IMAGE_KEY, false)) {
                setBlockNetworkImage();
            }
        }
    }

    private void setBlockNetworkImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:(function(){ var imgs=document.getElementsByTagName('img');" +
                                "for(i=0;i<imgs.length;i++) { imgs[i].style.display='none'; } })()");
                    }
                });
            }
        }).start();
    }

}

