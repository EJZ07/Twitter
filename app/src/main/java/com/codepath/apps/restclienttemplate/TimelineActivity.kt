package com.codepath.apps.restclienttemplate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Headers
import org.json.JSONException


@Suppress("DEPRECATION")
class TimelineActivity : AppCompatActivity() {

    lateinit var client: TwitterClient

    lateinit var rvTweets: RecyclerView

    lateinit var  adapter: TweetsAdapter

    lateinit var swipeContainer: SwipeRefreshLayout

    private var scrollListener: EndlessRecyclerViewScrollListener? = null

    val tweets = ArrayList<Tweet>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)


        supportActionBar?.setDisplayShowTitleEnabled(false)
       // supportActionBar?.setDisplayShowHomeEnabled(true)
       // supportActionBar?.setLogo(R.drawable.twitter_logo_svg)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_image);


        client = TwitterApplication.getRestClient(this)

        swipeContainer = findViewById(R.id.swipeContainer)



        swipeContainer.setOnRefreshListener {
            Log.i(TAG, "Refreshing timeline")
            populateHomeTimeline()
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );


        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetsAdapter(tweets)


        rvTweets.layoutManager = LinearLayoutManager(this)
        rvTweets.adapter = adapter



        populateHomeTimeline()

        scrollListener = object : EndlessRecyclerViewScrollListener(rvTweets.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                populateHomeTimeline()
            }
        }

        rvTweets.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListener)

    }

    //TODO
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //Handles clicks on menu items
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item?.itemId == R.id.compose){
//            val intent = Intent(this, ComposeActivity::class.java)
//            startActivityForResult(intent, REQUEST_CODE)
//        }
//
//        return super.onOptionsItemSelected(item)
//    }

    //this method is called when we come back from ComposeActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // If the user comes back to this activity from EditActivity
        // with no error or cancellation
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {

            val tweet = data?.getParcelableExtra("tweet") as Tweet?
            //Update timeline
            //Modifying the data source of tweets
            if (tweet != null) {
                tweets.add(0, tweet)
            }
            //update adapter
            adapter.notifyItemInserted( 0)
            rvTweets.smoothScrollToPosition(0)

        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    fun populateHomeTimeline() {
        client.getHomeTimeline(object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "onSuccess!")

                val jsonArray = json.jsonArray

                try {
                    //clear out our current tweets
                    adapter.clear()
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)

                    adapter.notifyDataSetChanged()

                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false)

                    scrollListener?.resetState()

                }catch (e: JSONException) {
                    Log.e(TAG, "JSON Exception $e")
                }


            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "onFailure $statusCode")
            }

        })
    }

    companion object {
        val TAG = "TimeLineActivity"
        val REQUEST_CODE = 10
    }

    fun onFabClick(view: View) {
        val intent = Intent(this, ComposeActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)

    }
}