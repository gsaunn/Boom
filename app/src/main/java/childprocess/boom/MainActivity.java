package childprocess.boom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {
private String Cur_song;
    private int Cur_index;
    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageView btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private ImageView albumart;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songArtiste;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    Typeface customfont;
    // Media Player
    private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
    private SongsManager songManager;
    private MiscUtilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ImageView btnShare;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences("theme_id", MODE_PRIVATE);
        String theme_id = prefs.getString("id", null);

        if (theme_id !=null  ){
            //check if the current Activity needs to be skipped {
            RelativeLayout root=(RelativeLayout)findViewById(R.id.root);
            root.setBackgroundResource(GetSkinid(theme_id));
            //
        }
        customfont=Typeface.createFromAsset(this.getAssets(), "fonts/noble.ttf");
        albumart = (ImageView) findViewById(R.id.albumart);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnShare = (ImageView) findViewById(R.id.btn_share);
        btnPlaylist = (ImageView) findViewById(R.id.btn_playlist);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songArtiste= (TextView) findViewById(R.id.songArtiste);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        songTitleLabel.setTypeface(customfont);
        songArtiste.setTypeface(customfont);
        songTitleLabel.setSelected(true);
        songCurrentDurationLabel.setTypeface(customfont);
        songTotalDurationLabel.setTypeface(customfont);
//        // Mediaplayer
        mp = new MediaPlayer();
        songManager = new SongsManager();
        utils = new MiscUtilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        // Getting all songs list
        songsList = songManager.getPlayList(getApplicationContext());
//        String Last_song = savedInstanceState.getString("SongPath");
//       int myInt = savedInstanceState.getInt("SongIndex");


        // By default play first song
      //  playSong(0);

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if(mp.isPlaying()){
                    if(mp!=null){
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                }else{
                    // Resume song
                    if(mp!=null){
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if(currentPosition + seekForwardTime <= mp.getDuration()){
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                }else{
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
            }
        });
        /**
         * Share over bluetooth test event listener
         * Hope it works
         * */
        btnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                if(Cur_song!=null){
                ShareOverBt(Cur_song);
            }
        }
                                    });
        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - seekBackwardTime >= 0){
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                }else{
                    // backward to starting position
                    mp.seekTo(0);
                }

            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if(currentSongIndex < (songsList.size() - 1)){
                    playSong(currentSongIndex + 1,null);
                    currentSongIndex = currentSongIndex + 1;
                }else{
                    // play first song
                    playSong(0,null);
                    currentSongIndex = 0;
                }

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(currentSongIndex > 0){
                    playSong(currentSongIndex - 1,null);
                    currentSongIndex = currentSongIndex - 1;
                }else{
                    // play last song
                    playSong(songsList.size() - 1,null);
                    currentSongIndex = songsList.size() - 1;
                }

            }
        });

        /**
         * Button Click event for Repeat button

         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isRepeat){
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }else{
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
            }
        });

        /**
         * Button shuffle button

         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
        btnPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
                startActivityForResult(i, 100);
            }
        });

    }

    public int GetSkinid(String id) {
        int ret_id;
        switch (Integer.parseInt(id)) {

            case 0:
                ret_id = R.drawable.bluebg3;
                break;
            case 1:
                ret_id = R.drawable.bluebg2;
                break;
            case 2:
                ret_id = R.drawable.bluebg1;
                break;
            case 3:
                ret_id = R.drawable.skin1;
                break;
            case 4:
                ret_id = R.drawable.skin2;
                break;
            case 5:
                ret_id = R.drawable.skin5;
                break;
            case 6:
                ret_id = R.drawable.skin3;
                break;
            case 7:
                ret_id = R.drawable.skin7;
                break;
            default:
                ret_id = R.drawable.bluebg4;
                break;

        }
        return ret_id;
    }
    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
            playSong(currentSongIndex,null);
        }
    }
    private void SetAlbumart(ImageView imageview,int songIndex,String songPath){
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songsList.get(songIndex).get("songPath"));

        byte [] data = mmr.getEmbeddedPicture();


        // convert the byte array to a bitmap
        if(data != null)
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            imageview.setImageBitmap(bitmap); //associated cover art in bitmap
            imageview.setAdjustViewBounds(true);
            imageview.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        }
        else
        {
            imageview.setImageResource(R.drawable.boom); //any default cover resourse folder
            imageview.setAdjustViewBounds(true);
            imageview.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        }
    }

    /**
     * Function to play a song
     * @param songIndex - index of song
     * */

    public void  playSong(int songIndex,String path){
        // Play song
        try {
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).get("songPath"));
            mp.prepare();
            mp.start();
            // Displaying Song title
            String songTitle = songsList.get(songIndex).get("songTitle");
            String songArtist=songsList.get(songIndex).get("songArtist");
            songTitleLabel.setText(songTitle);
            songArtiste.setText(songArtist);
            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            SetAlbumart(albumart, songIndex, songsList.get(songIndex).get("songPath"));
            Cur_song=songsList.get(songIndex).get("songPath"); //saves the current song path
            Cur_index=songIndex;
            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void  playLastSong(int songIndex,String path){
        // Play song
        try {
            mp.reset();
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
            // Displaying Song title
            String songTitle = songsList.get(songIndex).get("songTitle");
            String songArtist=songsList.get(songIndex).get("songArtist");
            songTitleLabel.setText(songTitle);

            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            SetAlbumart(albumart, songIndex, songsList.get(songIndex).get("songPath"));
            Cur_song=songsList.get(songIndex).get("songPath"); //saves the current song path
            Cur_index=songIndex;
            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }
    private void ShareOverBt(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        File Tosend = new File(path);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(Tosend));
        ///////////////////pakage manager

        startActivity(intent);
    }

        /**
    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     * */
    @Override
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if(isRepeat){
            // repeat is on play same song again
            playSong(currentSongIndex,null);
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex,null);
        } else{
            // no repeat or shuffle ON - play next song
            if(currentSongIndex < (songsList.size() - 1)){
                playSong(currentSongIndex + 1,null);
                currentSongIndex = currentSongIndex + 1;
            }else{
                // play first song
                playSong(0,null);
                currentSongIndex = 0;
            }
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mp.release();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.navi_option) {
            Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
            startActivityForResult(i, 100);
            return true;
        }
        if (id == R.id.pref_option) {
            Intent i = new Intent(getApplicationContext(), Pref_Activity.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.albumart_option) {
            View album = findViewById(R.id.albumart);
            if (album.getVisibility()==View.VISIBLE){
            album.setVisibility(View.GONE);
            }
            else{

                album.setVisibility(View.VISIBLE);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
