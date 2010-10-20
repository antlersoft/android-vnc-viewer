/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import com.antlersoft.android.db.*;

/**
 * @author Michael A. MacDonald
 *
 */
@TableInterface(ImplementingClassName="AbstractConnectionBean",TableName="CONNECTION_BEAN")
interface IConnectionBean {
	@FieldAccessor
	long get_Id();
	@FieldAccessor
	String getNickname();
	@FieldAccessor
	String getAddress();
	@FieldAccessor
	int getPort();
	@FieldAccessor
	String getPassword();
	@FieldAccessor
	String getColorModel();
	@FieldAccessor
	boolean getForceFull();
	@FieldAccessor
	String getRepeaterId();
	@FieldAccessor
	String getInputMode();
	@FieldAccessor(Name="SCALEMODE")
	String getScaleModeAsString();
	@FieldAccessor
	boolean getUseLocalCursor();
	@FieldAccessor
	boolean getKeepPassword();
	@FieldAccessor
	boolean getFollowMouse();
	@FieldAccessor
	boolean getUseRepeater();
	@FieldAccessor
	long getMetaListId();
	@FieldAccessor(Name="LAST_META_KEY_ID")
	long getLastMetaKeyId();
	@FieldAccessor(DefaultValue="0")
	boolean getFollowPan();
	@FieldAccessor
	String getUserName();
	@FieldAccessor
	String getSecureConnectionType();
}
