def can_build(env, platform):
	return platform=="android"

def configure(env):
	if (env['platform'] == 'android'):
		
		env.android_add_java_dir("android")
		
		env.android_add_res_dir("res")
		env.android_add_asset_dir("assets")
		
		env.android_add_dependency("implementation files('../../../modules/oppo/android/libs/gamesdk-20190227.jar')")
		env.android_add_dependency("implementation files('../../../modules/oppo/android/libs/oppo_mobad_api_v301_2018_12_07_release.jar')")
		
		
		env.android_add_default_config("applicationId 'com.opos.mobaddemo'")
		env.android_add_to_manifest("android/AndroidManifestChunk.xml")
		env.android_add_to_permissions("android/AndroidPermissionsChunk.xml")
		env.disable_module()

	