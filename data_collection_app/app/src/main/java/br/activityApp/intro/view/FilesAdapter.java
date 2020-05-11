package br.activityApp.intro.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.activityApp.R;
import br.activityApp.data.local.FileItem;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {
    private List<FileItem> filesList = new ArrayList<>();
    private Long lastSync;

    public FilesAdapter(Long lastSync) {
        this.lastSync = lastSync;
    }

    public void setFilesList(List<FileItem> filesList) {
        this.filesList = filesList;
    }

    public void setLastSync(Long lastSync) {
        this.lastSync = lastSync;
        notifyDataSetChanged();
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        FileItem item = filesList.get(position);
        holder.bindItem(item);
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView timestamp;
        private TextView sensor;
        private ImageView status;

        public FileViewHolder(View itemView) {
            super(itemView);

            this.name = (TextView) itemView.findViewById(R.id.txt_name);
            this.timestamp = (TextView) itemView.findViewById(R.id.txt_timestamp);
            this.sensor = (TextView) itemView.findViewById(R.id.txt_sensor);
            this.status = (ImageView) itemView.findViewById(R.id.img_status);
        }

        public void bindItem(FileItem item) {
            this.name.setText(item.getName());
            this.timestamp.setText(item.getPrettyDate());
            this.sensor.setText(item.getSensor());

            if (item.isSynced(lastSync)) {
                this.status.setImageResource(R.drawable.ic_done);
            } else {
                this.status.setImageResource(R.drawable.ic_cloud_upload);
            }
        }
    }
}
