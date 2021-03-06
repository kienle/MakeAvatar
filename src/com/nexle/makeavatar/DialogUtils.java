package com.nexle.makeavatar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.provider.Settings;

public class DialogUtils {
	
	public static Dialog createConfirmDialog(Context context, DialogInterface.OnClickListener onClickListenner, String message){
		return new AlertDialog.Builder(context).setMessage(message)
			.setCancelable(false)
			.setPositiveButton(R.string.ok, onClickListenner)
			.setNegativeButton(R.string.cancel, null).create();
	}
	
	public static Dialog createConfirmDialog(Context context, DialogInterface.OnClickListener onClickListenner, int messageId){
		return new AlertDialog.Builder(context).setMessage(messageId)
			.setCancelable(false)
			.setPositiveButton(R.string.ok, onClickListenner)
			.setNegativeButton(R.string.cancel, null).create();
	}
	
	public static Dialog createConfirmExistDialog(Context context,
            DialogInterface.OnClickListener onClickListenner,
            int messageId) {
        return new AlertDialog.Builder(context).setMessage(messageId)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, onClickListenner)
                .setNegativeButton(R.string.no, null).show();
    }
	
	public static Dialog createWarningDialog(Context context, DialogInterface.OnClickListener onClickListenner, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setTitle(title)
			.setIcon(android.R.drawable.stat_sys_warning)
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton(R.string.ok, onClickListenner);
		
		return builder.create();
	}
	
	public static Dialog createWarningDialog(Context context, int messageId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
		.setIcon(android.R.drawable.stat_sys_warning)
		.setMessage(messageId)
		.setCancelable(false)
		.setPositiveButton(R.string.ok, null);
		
		return builder.create();
	}
	
	public static Dialog createWarningDialog(Context context, int titleId, int messageId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setTitle(titleId)
			.setIcon(android.R.drawable.stat_sys_warning)
			.setMessage(messageId).setCancelable(false)
			.setPositiveButton(R.string.ok, null);
		
		return builder.create();
	}
	
	public static ProgressDialog createProgressDialog(Context context, int messageId) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(StringUtils.getString(messageId));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		return progressDialog;
	}
	
    public static ProgressDialog createProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }
    
    public static Dialog createNoNetworkDialog(final Activity context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setTitle("Network Not Connected")
			.setIcon(android.R.drawable.stat_sys_warning)
			.setMessage("Please connect to a network and try again")
			.setCancelable(false)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					context.finish();
				}
			});
		
		return builder.create();
	}
    
    public static Dialog createNoNetworkDialogForHome(final Activity context, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setTitle("Network Not Connected")
			.setIcon(android.R.drawable.stat_sys_warning)
			.setMessage("Please connect to a network and try again")
			.setCancelable(false)
			.setPositiveButton(R.string.ok, listener);
		
		return builder.create();
	}
    
    public static Dialog createServerErrorDialog(final Activity context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setTitle("Server is error or song does not exist on server.")
			.setIcon(android.R.drawable.stat_sys_warning)
			.setMessage("Please try again later")
			.setCancelable(false)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					context.finish();
				}
			});
		
		return builder.create();
	}
    
    /**
     * Function to show settings alert dialog On pressing Settings button will lauch Settings Options
     * */
    public static void showSettingsAlert(final Activity context, int title, int message, final String action) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(action);
                context.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (action == Settings.ACTION_LOCATION_SOURCE_SETTINGS) {
                    context.finish();
                }
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
    
    public static AlertDialog buildAlertDialog(Context context, int titleId, int messageId, boolean isCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setNeutralButton("OK", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (isCancel) {
            builder.setCancelable(true);
        }
        return builder.create();
    }
}
