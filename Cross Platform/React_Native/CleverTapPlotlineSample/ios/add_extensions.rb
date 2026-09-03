require 'xcodeproj'

project_path = 'CleverTapPlotlineSample.xcodeproj'
project = Xcodeproj::Project.open(project_path)

main_target = project.targets.find { |t| t.name == 'CleverTapPlotlineSample' }
raise 'Main target not found' unless main_target

def add_extension_target(project, main_target, name, product_bundle_id, sources, plist)
  existing = project.targets.find { |t| t.name == name }
  return existing if existing

  target = project.new_target(:app_extension, name, :ios, '12.0')
  target.product_name = name

  # Build configuration
  config = target.build_configurations.find { |c| c.name == 'Debug' }
  release_config = target.build_configurations.find { |c| c.name == 'Release' }
  [config, release_config].each do |c|
    c.build_settings['PRODUCT_BUNDLE_IDENTIFIER'] = product_bundle_id
    c.build_settings['INFOPLIST_FILE'] = plist
    c.build_settings['SDKROOT'] = 'iphoneos'
    c.build_settings['TARGETED_DEVICE_FAMILY'] = '1,2'
    c.build_settings['SWIFT_VERSION'] = '5.0'
    c.build_settings['GENERATE_INFOPLIST_FILE'] = 'NO'
    c.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '12.0'
    c.build_settings['MARKETING_VERSION'] = '1.0'
    c.build_settings['CURRENT_PROJECT_VERSION'] = '1'
    c.build_settings['CODE_SIGN_STYLE'] = 'Automatic'
    c.build_settings['LD_RUNPATH_SEARCH_PATHS'] = '$(inherited) @executable_path/Frameworks @executable_path/../../Frameworks'
  end

  sources.each do |path|
    ref = project.main_group.new_file(path)
    target.source_build_phase.add_file_reference(ref)
  end

  # Embed App Extensions into main target
  embed_phase = main_target.new_copy_files_build_phase("Embed #{name}")
  embed_phase.dst_subfolder_spec = '13' # PlugIns
  embed_phase.add_file_reference(target.product_reference)

  # Add Embed Foundation Extensions / run dependencies
  main_target.add_dependency(target)

  target
end

add_extension_target(
  project,
  main_target,
  'NotificationService',
  'com.clevertapplotlinesample.NotificationService',
  ['NotificationService/NotificationService.swift'],
  'NotificationService/Info.plist',
)

add_extension_target(
  project,
  main_target,
  'NotificationContent',
  'com.clevertapplotlinesample.NotificationContent',
  [
    'NotificationContent/NotificationViewController.swift',
    'NotificationContent/MainInterface.storyboard',
  ],
  'NotificationContent/Info.plist',
)

project.save
puts 'Extension targets added successfully'