package com.example.labthreadsasynctask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatusLabel;
    private ProgressBar pbTaskProgress;
    private ImageView ivFinalIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [UI THREAD] Initialisation des composants avec les nouveaux IDs
        tvStatusLabel = findViewById(R.id.tv_status_label);
        pbTaskProgress = findViewById(R.id.pb_task_progress);
        ivFinalIcon = findViewById(R.id.iv_final_icon);
        
        Button btnThreadAction = findViewById(R.id.btn_thread_action);
        Button btnAsynctaskAction = findViewById(R.id.btn_asynctask_action);
        Button btnUiCheck = findViewById(R.id.btn_ui_check);

        // 1. Approche Thread + Handler
        btnThreadAction.setOnClickListener(v -> {
            tvStatusLabel.setText("Statut : Téléchargement du rapport...");
            pbTaskProgress.setVisibility(View.VISIBLE);
            pbTaskProgress.setIndeterminate(true);
            ivFinalIcon.setImageDrawable(null);

            new Thread(() -> {
                try {
                    // [WORKER THREAD] Simulation d'une tâche de fond
                    Thread.sleep(2000); 

                    new Handler(Looper.getMainLooper()).post(() -> {
                        // [UI THREAD] Mise à jour de l'interface
                        tvStatusLabel.setText("Statut : Rapport téléchargé");
                        pbTaskProgress.setVisibility(View.INVISIBLE);
                        ivFinalIcon.setImageResource(android.R.drawable.ic_menu_save);
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        // 2. Approche AsyncTask
        btnAsynctaskAction.setOnClickListener(v -> {
            new SecurityScanTask(this).execute();
        });

        // 3. Test de réactivité
        btnUiCheck.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "UI réactive : Je peux cliquer !", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * AsyncTask statique pour éviter les fuites de mémoire.
     */
    private static class SecurityScanTask extends AsyncTask<Void, Integer, String> {
        private final WeakReference<MainActivity> activityRef;

        SecurityScanTask(MainActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            MainActivity activity = activityRef.get();
            if (activity != null) {
                activity.tvStatusLabel.setText("Statut : Analyse de sécurité...");
                activity.pbTaskProgress.setVisibility(View.VISIBLE);
                activity.pbTaskProgress.setIndeterminate(false);
                activity.pbTaskProgress.setProgress(0);
                activity.ivFinalIcon.setImageDrawable(null);
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            // [WORKER THREAD] Boucle de scan
            for (int i = 1; i <= 100; i++) {
                try {
                    // Simulation d'un calcul complexe
                    double mathWork = Math.sqrt(Math.pow(i, 2) + Math.sin(i));
                    Thread.sleep(50);
                    publishProgress(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "Scan terminé avec succès";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            MainActivity activity = activityRef.get();
            if (activity != null) {
                activity.pbTaskProgress.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            MainActivity activity = activityRef.get();
            if (activity != null) {
                activity.tvStatusLabel.setText("Statut : " + result);
                activity.ivFinalIcon.setImageResource(android.R.drawable.ic_dialog_info);
            }
        }
    }
}
