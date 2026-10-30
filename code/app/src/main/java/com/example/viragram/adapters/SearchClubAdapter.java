package com.example.viragram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.activities.ClubDetailActivity;
import com.example.viragram.databinding.ItemSearchClubBinding;
import com.example.viragram.models.Club;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying campus clubs in Search & Explore.
 */
public class SearchClubAdapter extends RecyclerView.Adapter<SearchClubAdapter.ClubViewHolder> {

    private final List<Club> clubList = new ArrayList<>();

    public void setClubs(List<Club> clubs) {
        this.clubList.clear();
        if (clubs != null) {
            this.clubList.addAll(clubs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchClubBinding binding = ItemSearchClubBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ClubViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {
        holder.bind(clubList.get(position));
    }

    @Override
    public int getItemCount() {
        return clubList.size();
    }

    static class ClubViewHolder extends RecyclerView.ViewHolder {
        private final ItemSearchClubBinding binding;

        public ClubViewHolder(@NonNull ItemSearchClubBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Club club) {
            Context context = itemView.getContext();

            if (club.getImageUrl() != null && !club.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(club.getImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_1)
                        .error(R.drawable.ic_avatar_1)
                        .into(binding.ivClubAvatar);
            } else if (club.getAvatarResId() != 0) {
                binding.ivClubAvatar.setImageResource(club.getAvatarResId());
            } else {
                binding.ivClubAvatar.setImageResource(R.drawable.ic_avatar_1);
            }

            binding.tvClubName.setText(club.getName());
            binding.tvClubDescription.setText(club.getDescription());
            binding.tvClubMembers.setText(club.getMemberCount() + " Active Members • Goa University");

            updateFollowUi(context, club);

            binding.btnClubFollow.setOnClickListener(v -> {
                club.setFollowing(!club.isFollowing());
                updateFollowUi(context, club);
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ClubDetailActivity.class);
                intent.putExtra(ClubDetailActivity.EXTRA_CLUB_NAME, club.getName());
                intent.putExtra(ClubDetailActivity.EXTRA_CLUB_DESC, club.getDescription());
                intent.putExtra(ClubDetailActivity.EXTRA_CLUB_MEMBERS, club.getMemberCount());
                intent.putExtra(ClubDetailActivity.EXTRA_AVATAR_RES, club.getAvatarResId());
                context.startActivity(intent);
            });
        }

        private void updateFollowUi(Context context, Club club) {
            if (club.isFollowing()) {
                binding.btnClubFollow.setText(R.string.btn_following);
                binding.btnClubFollow.setTextColor(ContextCompat.getColor(context, R.color.viragram_text_secondary));
                binding.btnClubFollow.setStrokeColorResource(R.color.viragram_border);
            } else {
                binding.btnClubFollow.setText(R.string.btn_follow);
                binding.btnClubFollow.setTextColor(ContextCompat.getColor(context, R.color.viragram_purple_primary));
                binding.btnClubFollow.setStrokeColorResource(R.color.viragram_purple_primary);
            }
        }
    }
}