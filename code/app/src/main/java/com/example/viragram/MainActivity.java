package com.example.viragram;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.viragram.activities.CollegeHubActivity;
import com.example.viragram.activities.LoginActivity;
import com.example.viragram.activities.MessagesActivity;
import com.example.viragram.databinding.ActivityMainBinding;
import com.example.viragram.firebase.FirebaseAuthManager;

/**
 * Main entry point for the Viragram application, controlling top-level bottom navigation.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        binding.topToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_college_hub) {
                Intent intent = new Intent(this, CollegeHubActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_messages) {
                Intent intent = new Intent(this, MessagesActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_notifications) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_notifications);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Redirect unauthenticated or unverified users to LoginActivity
        if (!FirebaseAuthManager.getInstance().isUserSignedIn() || !FirebaseAuthManager.getInstance().isEmailVerified()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}