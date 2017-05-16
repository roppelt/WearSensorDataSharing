package com.biankaroppelt.masterthesis;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.squareup.otto.Subscribe;

public class MobileActivity extends AppCompatActivity {

   private static final String TAG = MobileActivity.class.getSimpleName();
   private Button buttonNewMainStudy;
   private Button buttonNewPilotStudy1A;
   private Button buttonNewPilotStudy1B;
   private Button buttonNewPilotStudy2;
   private CoordinatorLayout coordinatorLayout;

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar.make(coordinatorLayout, R.string.no_watch_paired, Snackbar.LENGTH_LONG)
            .show();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_mobile);
      buttonNewPilotStudy1A = ((Button) findViewById(R.id.button_new_pilot_study_1a));
      buttonNewPilotStudy1B = ((Button) findViewById(R.id.button_new_pilot_study_1b));
      buttonNewPilotStudy2 = ((Button) findViewById(R.id.button_new_pilot_study_2));
      buttonNewMainStudy = ((Button) findViewById(R.id.button_new_main_study));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      setupToolbar();
      setupButtonListener();
   }

   @Override
   protected void onPause() {
      super.onPause();
      BusProvider.getInstance()
            .unregister(this);
   }

   @Override
   protected void onResume() {
      super.onResume();
      BusProvider.getInstance()
            .register(this);
   }

   private void setupButtonListener() {
      buttonNewPilotStudy1A.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(MobileActivity.this, NewPilotStudy1AActivity.class);
            startActivity(intent);
         }
      });
      buttonNewPilotStudy1B.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(MobileActivity.this, NewPilotStudy1BActivity.class);
            startActivity(intent);
         }
      });
      buttonNewPilotStudy2.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(MobileActivity.this, NewPilotStudy2Activity.class);
            startActivity(intent);
         }
      });
      buttonNewMainStudy.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(MobileActivity.this, NewMainStudyActivity.class);
            startActivity(intent);
         }
      });
   }

   private void setupToolbar() {
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
   }
}
