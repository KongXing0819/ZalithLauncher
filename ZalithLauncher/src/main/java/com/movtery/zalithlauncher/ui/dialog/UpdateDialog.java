package com.movtery.zalithlauncher.ui.dialog;

import static com.movtery.zalithlauncher.utils.stringutils.StringUtils.markdownToHtml;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.movtery.zalithlauncher.feature.UpdateLauncher;
import com.movtery.zalithlauncher.setting.Settings;
import com.movtery.zalithlauncher.utils.ZHTools;
import com.movtery.zalithlauncher.utils.file.FileTools;
import com.movtery.zalithlauncher.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.DialogUpdateBinding;

public class UpdateDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final DialogUpdateBinding binding = DialogUpdateBinding.inflate(getLayoutInflater());
    private final String versionName;
    private final String tagName;
    private final String createdTime;
    private final long fileSize;
    private final String description;

    public UpdateDialog(@NonNull Context context, UpdateInformation updateInformation) {
        super(context);
        this.versionName = updateInformation.versionName;
        this.tagName = updateInformation.tagName;
        this.createdTime = updateInformation.createdTime;
        this.fileSize = updateInformation.fileSize;
        this.description = updateInformation.description;

        this.setCancelable(false);
        this.setContentView(binding.getRoot());
        init();
        DraggableDialog.initDialog(this);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        String version = StringUtils.insertSpace(getContext().getString(R.string.update_dialog_version), this.versionName);
        String time = StringUtils.insertSpace(getContext().getString(R.string.update_dialog_time), this.createdTime);
        String size = StringUtils.insertSpace(getContext().getString(R.string.update_dialog_file_size), FileTools.formatFileSize(this.fileSize));

        binding.versionName.setText(version);
        binding.updateTime.setText(time);
        binding.fileSize.setText(size);

        String descriptionHtml = markdownToHtml(this.description);

        ZHTools.getWebViewAfterProcessing(binding.description);

        binding.description.getSettings().setJavaScriptEnabled(true);
        binding.description.loadDataWithBaseURL(null, descriptionHtml, "text/html", "UTF-8", null);

        binding.updateButton.setOnClickListener(view -> {
            this.dismiss();
            if (ZHTools.areaChecks("zh")) {
                runOnUiThread(() -> {
                    UpdateSourceDialog updateSourceDialog = new UpdateSourceDialog(getContext(), versionName, tagName, fileSize);
                    updateSourceDialog.show();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.update_downloading_tip, "Github Release"), Toast.LENGTH_SHORT).show());
                UpdateLauncher updateLauncher = new UpdateLauncher(getContext(), versionName, tagName, fileSize, UpdateLauncher.UpdateSource.GITHUB_RELEASE);
                updateLauncher.start();
            }
        });
        binding.cancelButton.setOnClickListener(view -> this.dismiss());
        binding.ignoreButton.setOnClickListener(view -> {
            Settings.Manager.Companion.put("ignoreUpdate", this.versionName).save();
            this.dismiss();
        });
    }

    @Override
    public Window onInit() {
        return getWindow();
    }

    public static class UpdateInformation {
        public String versionName;
        public String tagName;
        public String createdTime;
        public long fileSize;
        public String description;

        public void information(@NonNull String versionName, @NonNull String tagName, @NonNull String createdTime, long fileSize, @NonNull String description) {
            this.versionName = versionName;
            this.tagName = tagName;
            this.createdTime = createdTime;
            this.fileSize = fileSize;
            this.description = description;
        }
    }
}