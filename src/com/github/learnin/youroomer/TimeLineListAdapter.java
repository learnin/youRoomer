package com.github.learnin.youroomer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.http.HttpStatus;

import youroom4j.YouRoomClient;
import youroom4j.http.HttpResponseEntity;
import youroom4j.http.HttpResponseHandler;
import youroom4j.oauth.OAuthTokenCredential;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TimeLineListAdapter extends ArrayAdapter<Entry> {

	private LayoutInflater mLayoutInflater;
	private TextView mUsername;
	private TextView mCreatedAt;
	private TextView mContent;
	private TextView mCommentCount;
	private ImageView mUserImage;
	private YouRoomClient mYouRoomClient;

	public TimeLineListAdapter(Context context, List<Entry> entryList) {
		super(context, 0, entryList);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SharedPreferences sharedPreferences = context.getSharedPreferences("oauth", Context.MODE_PRIVATE);
		OAuthTokenCredential oAuthTokenCredential = new OAuthTokenCredential();
		oAuthTokenCredential.setToken(sharedPreferences.getString("token", ""));
		oAuthTokenCredential.setTokenSecret(sharedPreferences.getString("tokenSecret", ""));
		mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		mYouRoomClient.setOAuthTokenCredential(oAuthTokenCredential);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.entry_row, null);
		}

		Entry entry = this.getItem(position);
		if (entry != null) {
			mUserImage = (ImageView) view.findViewById(R.id.user_image);

			// DefaultHttpClient dhc = new DefaultHttpClient();
			// HttpResponse httpResponse;
			// InputStream in = null;
			try {
				// FIXME 画像が表示されない
				Bitmap bitmap =
					mYouRoomClient.showPicture(
						entry.getParticipation().getUserImageURI(),
						new HttpResponseHandler<Bitmap>() {
							@Override
							public Bitmap handleResponse(HttpResponseEntity responseEntity) throws IOException {
								if (responseEntity.getStatusCode() == HttpStatus.SC_OK) {
									InputStream is = null;
									try {
										is = responseEntity.getContent();
										return BitmapFactory.decodeStream(is);
									} finally {
										if (is != null) {
											is.close();
										}
									}
								}
								BufferedReader br = null;
								try {
									br = new BufferedReader(new InputStreamReader(responseEntity.getContent(), "UTF-8"));
									StringBuilder result = new StringBuilder();
									String line;
									while ((line = br.readLine()) != null) {
										result.append(line);
									}
									System.out.println(result.toString());
								} finally {
									if (br != null) {
										try {
											br.close();
										} catch (IOException e) {
										}
									}
								}
								return null;
							}
						});
				mUserImage.setImageBitmap(bitmap);
				// httpResponse = dhc.execute(new
				// HttpGet(entry.getParticipation().getUserImageURI()));
				// if (httpResponse.getStatusLine().getStatusCode() ==
				// HttpStatus.SC_OK) {
				// HttpEntity httpEntity = httpResponse.getEntity();
				// in = httpEntity.getContent();
				// Bitmap bitm = BitmapFactory.decodeStream(in);
				// mUserImage.setImageBitmap(bitm);
				// // mUserImage.invalidate();
				// }
				// } catch (ClientProtocolException e) {
				// e.printStackTrace();
				// } catch (IOException e) {
				// e.printStackTrace();
				// } finally {
				// if (in != null) {
				// try {
				// in.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// }
				// }
			} catch (Exception e) {
				System.out.println(e);
			}

			mUsername = (TextView) view.findViewById(R.id.username);
			mUsername.setText(entry.getParticipation().getName());

			mCreatedAt = (TextView) view.findViewById(R.id.created_at);
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			mCreatedAt.setText(df.format(entry.getCreatedAt()));

			mContent = (TextView) view.findViewById(R.id.content);
			mContent.setText(entry.getContent());

			if (entry.getChildren() != null) {
				mCommentCount = (TextView) view.findViewById(R.id.comment_count);
				String mCommentCountText = entry.getChildren().size() + " comment";
				if (entry.getChildren().size() > 1) {
					mCommentCountText += "s";
				}
				mCommentCount.setText(mCommentCountText);
			}
		}
		return view;
	}
}
