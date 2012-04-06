package com.lazydroid.autoupdateapk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;

public class AutoUpdateApkActivity extends Activity {

	// instantiate updater class here (or in the Application)
	private AutoUpdateApk aua;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		aua = new AutoUpdateApk(getApplicationContext());

		PackageManager pm = this.getPackageManager();
		String packageName = this.getPackageName();
		int flags = PackageManager.GET_SIGNATURES;
		PackageInfo packageInfo = null;

		try {
			packageInfo = pm.getPackageInfo(packageName, flags);
		} catch (NameNotFoundException e) {
			// TODO some error checking
			e.printStackTrace();
		}
		Signature[] signatures = packageInfo.signatures;

		// cert = DER encoded X.509 certificate:
		byte[] cert = signatures[0].toByteArray();
		Log.i("cert", "length: " + cert.length);
		InputStream input = new ByteArrayInputStream(cert);

		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X509");
		} catch (CertificateException e) {
			// TODO some error checking
			e.printStackTrace();
		}
		X509Certificate c = null;
		try {
			c = (X509Certificate) cf.generateCertificate(input);
		} catch (CertificateException e) {
			// TODO some error checking
			e.printStackTrace();
		}
		Log.d("Example", "Certificate for: " + c.getSubjectDN());
		Log.d("Example", "Certificate issued by: " + c.getIssuerDN());
		Log.d("Example", "The certificate is valid from " + c.getNotBefore() + " to " + c.getNotAfter());
		Log.d("Example", "Certificate SN# " + c.getSerialNumber());
		Log.d("Example", "Generated with " + c.getSigAlgName());
	}
}