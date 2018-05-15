/*
 * Copyright (c) 2012-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.absyz;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Main activity
 */
public class MainActivity extends SalesforceActivity {

	private RestClient client;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup view
		setContentView(R.layout.main);
	}

	@Override
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);

		super.onResume();
	}

	@Override
	public void onResume(final RestClient client) {
		// Keeping reference to rest client
		this.client = client;
		System.out.println(client.getClientInfo());
		RestRequest restRequest = null;
		try {
			restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), "select id,name from account");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, final RestResponse result) {
				result.consumeQuietly(); // consume before going back to main thread
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							//this technique one is also showing error after one day.
							Toast.makeText(MainActivity.this,"Valid Session",Toast.LENGTH_LONG).show();
							String communityUrl= String.valueOf(client.getClientInfo().communityUrl);
							System.out.println("community URL"+client.getClientInfo().communityUrl);
							//String accessToken=client.getAuthToken();
							//instead of sending access token , we are sending refresh token , its working now , lets see after some time
							OpenWebView(communityUrl,client.getRefreshToken());

						} catch (Exception e) {
							onError(e);
						}
					}
				});
			}

			@Override
			public void onError(final Exception exception) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
								Toast.LENGTH_LONG).show();

						TriggerLogOut();
					}
				});
			}
		});
		//if we are using for Normal Salesforce org we use InstanceURl

		//need to check for token expiration
		//System.out.println("accessToken"+client.getAuthToken());
		// Show everything

	}

	private void TriggerLogOut() {
		SalesforceSDKManager.getInstance().logout(this);
	}

	public void OpenWebView(String communityUrl, String accessToken)
	{
		findViewById(R.id.root).setVisibility(View.VISIBLE);
		//this secure frontdoor is must
		String url = ""+ communityUrl +"/one/one.app?sid="+ accessToken +"";
		System.out.print("FinalURL:\n"+url+"\n");
		WebView webview = (WebView)findViewById(R.id.webView);
		webview.setWebViewClient(new WebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl(url);

	}

}