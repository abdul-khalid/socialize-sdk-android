/**
 * 
 */
package com.socialize.ui.provider;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialize.Socialize;
import com.socialize.android.ioc.IBeanFactory;
import com.socialize.entity.Comment;
import com.socialize.entity.User;
import com.socialize.log.SocializeLogger;
import com.socialize.ui.SocializeUI;
import com.socialize.ui.view.ListItemLoadingView;
import com.socialize.ui.view.ViewHolder;
import com.socialize.ui.widget.CommentListItem;
import com.socialize.util.Base64;
import com.socialize.util.Base64DecoderException;
import com.socialize.util.Drawables;
import com.socialize.util.StringUtils;

/**
 * Provides comments to the comment view.
 * @author jasonpolites
 *
 */
public class SocializeCommentProvider extends BaseAdapter {

	private IBeanFactory<CommentListItem> commentItemViewFactory;
	private IBeanFactory<ListItemLoadingView> listItemLoadingViewFactory;
	private List<Comment> comments;
	private SocializeLogger logger;
	private Drawables drawables;
	private View loadingView;
	private boolean last = false;
	
	public SocializeCommentProvider(Context context) {
		super();
	}

	@Override
	public int getCount() {
		int extra = 1;
		if(!isDisplayLoading()) {
			extra = 0;
		}
		return (comments == null) ? 0 : comments.size() + extra;
	}
	
	boolean isDisplayLoading() {
		return !(last || (comments != null && comments.size() == 0));
	}

	@Override
	public Object getItem(int position) {
		if(position < comments.size()) {
			return (comments == null) ? null : comments.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		Comment item = (Comment) getItem(position);
		return (item == null) ? -1 : item.getId();
	}
	
	@Override
	public int getItemViewType(int position) {
		if(!isDisplayLoading() || position < comments.size()) {
			return 0;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getViewTypeCount() {
		if(!isDisplayLoading()) {
			return 1;
		}
		else {
			return 2;
		}
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
        ViewHolder holder;
		
        if (view == null) {
        	
        	CommentListItem v = commentItemViewFactory.getBean();
        	
            holder = new ViewHolder();
            
            holder.time = v.getTime();
            holder.userName = v.getAuthor();
            holder.comment = v.getComment();
    		holder.userIcon =  v.getUserIcon();
    		holder.now = new Date();

            v.setTag(holder);
            
            view = v;
        } 
        else {
            holder = (ViewHolder) view.getTag();
        }
        
        if(position >= comments.size()) {
        	// Last one, get loading view
        	if(loadingView == null) {
        		loadingView = listItemLoadingViewFactory.getBean();
        		loadingView.setTag(holder);
        	}
        	
        	view = loadingView;
        }
        else {
        	Comment item = (Comment) getItem(position);
    		
    		if(item != null) {
    			User currentUser = Socialize.getSocialize().getSession().getUser();
    			User user = item.getUser();
    			
    			if(currentUser != null && user != null && currentUser.getId().equals(user.getId())) {
    				user = currentUser;
    			}
    			
    			if (holder.comment != null) {
    				holder.comment.setText(item.getText());
    			}
    			if (holder.userName != null) {
    				
    				if(user != null) {
    					holder.userName.setText(user.getDisplayName());
    				}
    				else {
    					holder.userName.setText("Anonymous");
    				}
    			}
    			if (holder.time != null) {
    				
    				Long date = item.getDate();
    				if(date != null && date > 0) {
    					long diff = holder.now.getTime() - date.longValue();
    					
    					if(diff > 60000) {
    						holder.time.setText("A while ago ");
    					}
    					else {
    						holder.time.setText("Just now ");
    					}
    				}
    				else {
    					holder.time.setText(" ");
    				}
    			}
    			if (holder.userIcon != null) {
    				if(user != null) {
    					if(!StringUtils.isEmpty(user.getSmallImageUri())) {
    						try {
    							Uri uri = Uri.parse(user.getSmallImageUri());
    							holder.userIcon.setImageURI(uri);
    						}
    						catch (Exception e) {
    							String errorMsg = "Not a valid image uri [" + user.getSmallImageUri() + "]";
    							if(logger != null) {
    								logger.error(errorMsg, e);
    							}
    							else {
    								System.err.println(errorMsg);
    							}
    							
    							holder.userIcon.setImageDrawable(SocializeUI.getInstance().getDrawable(SocializeUI.DEFAULT_USER_ICON));
    						}
    					}
    					else if(drawables != null && !StringUtils.isEmpty(user.getProfilePicData())) {
    						try {
								Drawable drawable = drawables.getDrawable(user.getId().toString(), Base64.decode(user.getProfilePicData()));
								holder.userIcon.setImageDrawable(drawable);
							}
							catch (Base64DecoderException e) {
								if(logger != null) {
									logger.error("Invalid image data", e);
								}
								else {
									e.printStackTrace();
								}
								
								holder.userIcon.setImageDrawable(SocializeUI.getInstance().getDrawable(SocializeUI.DEFAULT_USER_ICON));
							}
    					}
    					else {
    						holder.userIcon.setImageDrawable(SocializeUI.getInstance().getDrawable(SocializeUI.DEFAULT_USER_ICON));
    					}
    				}
    				else {
    					holder.userIcon.setImageDrawable(SocializeUI.getInstance().getDrawable(SocializeUI.DEFAULT_USER_ICON));
    				}
    			}
    		}
        }
        
		return view;
	}

	public IBeanFactory<CommentListItem> getCommentItemViewFactory() {
		return commentItemViewFactory;
	}

	public void setCommentItemViewFactory(IBeanFactory<CommentListItem> commentItemViewFactory) {
		this.commentItemViewFactory = commentItemViewFactory;
	}

	public IBeanFactory<ListItemLoadingView> getListItemLoadingViewFactory() {
		return listItemLoadingViewFactory;
	}

	public void setListItemLoadingViewFactory(IBeanFactory<ListItemLoadingView> listItemLoadingViewFactory) {
		this.listItemLoadingViewFactory = listItemLoadingViewFactory;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public SocializeLogger getLogger() {
		return logger;
	}

	public void setLogger(SocializeLogger logger) {
		this.logger = logger;
	}

	public View getLoadingView() {
		return loadingView;
	}

	public void setLoadingView(View loadingView) {
		this.loadingView = loadingView;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public Drawables getDrawables() {
		return drawables;
	}

	public void setDrawables(Drawables drawables) {
		this.drawables = drawables;
	}
}
