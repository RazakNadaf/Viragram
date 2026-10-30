package com.example.viragram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viragram.R;
import com.example.viragram.activities.StudentProfileActivity;
import com.example.viragram.databinding.ItemSearchStudentBinding;
import com.example.viragram.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying student search and suggestion results.
 */
public class SearchStudentAdapter extends RecyclerView.Adapter<SearchStudentAdapter.StudentViewHolder> {

    private final List<User> studentList = new ArrayList<>();

    public void setStudents(List<User> students) {
        this.studentList.clear();
        if (students != null) {
            this.studentList.addAll(students);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchStudentBinding binding = ItemSearchStudentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(studentList.get(position));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemSearchStudentBinding binding;
        private boolean isFollowing = false;

        public StudentViewHolder(@NonNull ItemSearchStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User student) {
            Context context = itemView.getContext();

            try {
                int avatarRes = Integer.parseInt(student.getProfileImageUrl());
                binding.ivStudentAvatar.setImageResource(avatarRes);
            } catch (Exception e) {
                binding.ivStudentAvatar.setImageResource(R.drawable.ic_avatar_1);
            }

            binding.tvStudentName.setText(student.getFullName());
            binding.tvStudentUsername.setText("@" + student.getUsername().replace("@", ""));
            binding.tvStudentCourse.setText(student.getBio());

            updateFollowUi(context);

            binding.btnStudentFollow.setOnClickListener(v -> {
                isFollowing = !isFollowing;
                updateFollowUi(context);
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, StudentProfileActivity.class);
                intent.putExtra(StudentProfileActivity.EXTRA_NAME, student.getFullName());
                intent.putExtra(StudentProfileActivity.EXTRA_USERNAME, student.getUsername());
                intent.putExtra(StudentProfileActivity.EXTRA_COURSE, student.getBio());
                try {
                    intent.putExtra(StudentProfileActivity.EXTRA_AVATAR_RES, Integer.parseInt(student.getProfileImageUrl()));
                } catch (Exception ignored) {
                }
                context.startActivity(intent);
            });
        }

        private void updateFollowUi(Context context) {
            if (isFollowing) {
                binding.btnStudentFollow.setText(R.string.btn_following);
                binding.btnStudentFollow.setTextColor(ContextCompat.getColor(context, R.color.viragram_text_secondary));
                binding.btnStudentFollow.setStrokeColorResource(R.color.viragram_border);
            } else {
                binding.btnStudentFollow.setText(R.string.btn_follow);
                binding.btnStudentFollow.setTextColor(ContextCompat.getColor(context, R.color.viragram_purple_primary));
                binding.btnStudentFollow.setStrokeColorResource(R.color.viragram_purple_primary);
            }
        }
    }
}