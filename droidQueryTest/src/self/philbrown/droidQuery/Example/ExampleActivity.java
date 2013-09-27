/*
 * Copyright 2013 Phil Brown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.philbrown.droidQuery.Example;

import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import self.philbrown.droidProgress.Progress;
import self.philbrown.droidProgress.ProgressOptions;
import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.AjaxOptions;
import self.philbrown.droidQuery.AnimationOptions;
import self.philbrown.droidQuery.Function;
import self.philbrown.droidQuery.ViewObserver.Observation;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Example Activity shows how to use some components of 
 * <a href="https://github.com/phil-brown/droidQuery">droidQuery</a>, an Android port of jQuery.
 * <br>This example displays the public stream for <a href="http://app.net">App.net</a>.
 * @author Phil Brown
 *
 */
public class ExampleActivity extends Activity
{
	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.example);
        
        //droidQuery supports extensions/plug-ins.
        Map<String, String> extensions = null;
        
        try
        {
        	//this is the quickest way to make a Map of Objects:
        	extensions = (Map<String, String>) $.map("{" +
        			                                   "progress: 'self.philbrown.droidProgress.Progress', " +
        			                                   "mail:     'self.philbrown.droidMail.Mail'" +
        			                                  "}");
        }
        catch (Throwable t)
        {
        	//if you mess up the JSON, you may just want to stick with this, which is also fairly simple:
        	extensions = (Map<String, String>) $.map(
            		$.entry("progress", "self.philbrown.droidProgress.Progress"),
            		$.entry("mail", "self.philbrown.droidMail.Mail"));
        }
        
        
        
        //set droidQuery Extensions
        for (Entry<String, String> extension : extensions.entrySet())
        {
        	try
        	{
        		$.extend(extension.getKey(), extension.getValue());
        	}
        	catch (Throwable t)
        	{
        		if (extension.getKey() != null)
        			Log.w("Example", "Could not create extension " + extension.getKey());
        		else
        			Log.w("Example", "Could not create extension");
        	}
        }
        
        //use the Progress extension to show a progress spinner for global ajax events
        final Progress progress = (Progress) $.with(this).ext("progress", new ProgressOptions().indeterminate(true));
        
        $.ajaxStart(new Function() {
        	public void invoke($ droidQuery, Object... args)
        	{
        		Log.i("Ajax Test", "Global start");
        		progress.start();
        	}
        });
        
        $.ajaxStop(new Function(){
        	public void invoke($ droidQuery, Object... args)
        	{
        		Log.i("Ajax Test", "Global stop");
        		progress.stop();
        	}
        });
        
        //refresh the list.
        refresh();
        
        //Register a click event
        $.with(this, R.id.btn_refresh).click(new Function() {
			@Override
			public void invoke($ droidQuery, Object... params) {
				Toast.makeText(ExampleActivity.this, "Refresh", Toast.LENGTH_SHORT).show();
				refresh();
			}
        })
//        .observe("selected", new Function() {
//
//			@Override
//			public void invoke($ droidQuery, Object... params) {
//				Observation observation = (Observation) params[0];
//				Log.i("example", "button selected");
//				if (((Boolean) observation.newValue) == true)
//				{
//					Log.i("example", "button selected");
//					droidQuery.attr("backgroundColor", Color.WHITE);
//				}
//				else
//				{
//					Log.i("example", "button deselected");
//					droidQuery.attr("backgroundColor", Color.BLACK);
//				}
//				
//			}
//        	
//        }).observe("alpha", new Function() {
//        	@Override
//			public void invoke($ droidQuery, Object... params) {
//        		Log.i("$", "Alpha Changed");
//        	}
//        }).attr("backgroundColor", Color.BLACK).attr("alpha", 0.5f)
        ;
        
        //or use the "on" method to register a longClick event.
        $.with(this, R.id.btn_longrefresh).on("longClick", new Function() {
        	@Override
			public void invoke($ droidQuery, Object... params) {
        		droidQuery.toast("Refresh", Toast.LENGTH_LONG);
        		refresh();
			}
        });
        
	}
	
	//be sure to cancel all AsyncTasks when the app is destroyed, in order to prevent memory leaks.
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		$.ajaxKillAll();
	}
	
	/**
	 * Refreshes the list of cells containing App.net messages. This <em>ListView</em> is actually
	 * a <em>scrollable LinearLayout</em>, and is assembled in much the same way a layout would be
	 * made using <em>JavaScript</em>, with the <em>CSS3</em> attribute <em>overscroll-y: scroll</em>.
	 * <br>
	 * For this example, the public stream is retrieved using <em>ajax</em>, and for each message
	 * received, a new cell is created. For each cell, a new <em>ajax</em> request is started to
	 * retrieve the thumbnail image for the user. As all these events occur on a background thread, the
	 * main ScrollView is populated with cells and displayed to the user.
	 * <br>
	 * The stream <em>JSON</em> request is performed in a <em>global ajax</em> request, which will
	 * trigger the global start and stop events (which show a progress indicator, using a droidQuery
	 * extension). The image get requests are not global, so they will not trigger global events.
	 */
	public void refresh()
	{
		$.ajax(new AjaxOptions()
				.url("https://alpha-api.app.net/stream/0/posts/stream/global")
				.dataType("json")
				.type("GET")
				.error(new Function() {
					@Override
					public void invoke($ droidQuery, Object... params) {
						//Object error, int status, String reason
						Object error = params[0];
						int status = (Integer) params[1];
						String reason = (String) params[2];
						Log.w("app.net Client", "Could not complete request: " + reason);
					}
				})
				.success(new Function() {
					@Override
					public void invoke($ droidQuery, Object... params) 
					{
						//Object, reason
						JSONObject json = (JSONObject) params[0];
						String reason = (String) params[1];
						try 
						{
							Map<String, ?> map = $.map(json);
							JSONArray datas = (JSONArray) map.get("data");
							
							
							
							if (datas.length() != 0)
							{
								//clear old subviews in layout
								$.with(ExampleActivity.this, R.id.example_layout).selectChildren().remove();
								
								//get each message infos and create a cell
								for (int i = 0; i < datas.length(); i++) 
								{
									JSONObject jdata = (JSONObject) datas.get(i);
									Map<String, ?> data = $.map(jdata);
									
									String text = data.get("text").toString();
									
									Map<String, ?> user = $.map((JSONObject) data.get("user"));
									
									String username = user.get("username").toString();
									String avatarURL = ((JSONObject) user.get("avatar_image")).getString("url");
									
									//get Avatar image in a new task (but go ahead and create the cell for now)
									LinearLayout cell = new LinearLayout(ExampleActivity.this);
									LinearLayout.LayoutParams cell_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
									cell_params.bottomMargin = 5;
									cell.setLayoutParams(cell_params);
									cell.setOrientation(LinearLayout.HORIZONTAL);
									cell.setWeightSum(8);
									cell.setPadding(5, 5, 5, 5);
									cell.setBackgroundColor(Color.parseColor("#333333"));
									final LinearLayout fcell = cell;
									
									//contains the image location
									ImageView image = new ImageView(ExampleActivity.this);
									image.setId(99);
									LinearLayout.LayoutParams ip_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
									ip_params.weight = 2;
									image.setLayoutParams(ip_params);
									image.setPadding(0, 0, 5, 0);
									$.with(image).attr("alpha", 0.0f);
									cell.addView(image);
									final ImageView fimage = image;
									
									//the text location in the cell
									LinearLayout body = new LinearLayout(ExampleActivity.this);
									LinearLayout.LayoutParams body_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
									body_params.weight = 5;
									body.setLayoutParams(body_params);
									body.setOrientation(LinearLayout.VERTICAL);
									body.setGravity(Gravity.CENTER_VERTICAL);
									cell.addView(body);
									
									//the username
									TextView name = new TextView(ExampleActivity.this);
									LinearLayout.LayoutParams name_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
									name.setLayoutParams(name_params);
									name.setTextColor(Color.GRAY);
									name.setText(username);
									body.addView(name);
									
									//the message
									TextView message = new TextView(ExampleActivity.this);
									LinearLayout.LayoutParams msg_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
									message.setLayoutParams(msg_params);
									message.setTextColor(Color.WHITE);
									message.setTextSize(18);
									message.setText(text);
									body.addView(message);
									
									CheckBox checkbox = new CheckBox(ExampleActivity.this);
									LinearLayout.LayoutParams box_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
									box_params.weight = 1;
									checkbox.setLayoutParams(box_params);
									
									cell.addView(checkbox);
									
									$.with(ExampleActivity.this, R.id.example_layout).add(cell);
									//$.with(fimage).image(avatarURL, 200, 200, $.noop());
									$.ajax(new AjaxOptions(avatarURL)
												.type("GET")
												.dataType("image")
												.imageHeight(200)
												.imageWidth(200)
												.global(false)
												.success(new Function() {
													@Override
													public void invoke($ droidQuery, Object... params)
													{
														//Object, reason
														Bitmap src = (Bitmap) params[0];
														String reason = (String) params[1];
														$.with(fimage).val(src);
														try 
														{
															$.with(fimage).fadeIn(new AnimationOptions("{ duration: 400 }"));
														} catch (Throwable e) 
														{
															e.printStackTrace();
														}
														LinearLayout.LayoutParams lparams = (LinearLayout.LayoutParams) fcell.getLayoutParams();
														try 
														{
															lparams.height = Math.min(src.getWidth(), fimage.getWidth());
														}
														catch (Throwable t)
														{
															//ignore NPE
														}
														
														fcell.setLayoutParams(lparams);
													}
												})
												.error(new Function() {
													@Override
													public void invoke($ droidQuery, Object... params)
													{
														//Object error, int status, String reason
														Object error = params[0];
														int status = (Integer) params[1];
														String reason = (String) params[2];
														Log.w("app.net Client", "Could not complete image request: " + reason);
													}
												}));
										
								}
							}
							else
							{
								Log.w("app.net client", "could not update data");
							}
						}
						catch (Throwable t) 
						{
							t.printStackTrace();
						}
					}
				}));
	}
}
