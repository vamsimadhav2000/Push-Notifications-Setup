require 'xcodeproj'

project_path = 'CleverTapPlotlineSample.xcodeproj'
project = Xcodeproj::Project.open(project_path)

main_target = project.targets.find { |t| t.name == 'CleverTapPlotlineSample' }
raise 'Main target not found' unless main_target

main_target.build_configurations.each do |c|
  c.build_settings['CODE_SIGN_ENTITLEMENTS'] =
    'CleverTapPlotlineSample/CleverTapPlotlineSample.entitlements'
end

# Register the entitlement file in the project group (nice-to-have for Xcode UI)
group = project.main_group.find_subpath('CleverTapPlotlineSample', true)
unless group.files.any? { |f| f.path&.end_with?('.entitlements') }
  group.new_file('CleverTapPlotlineSample.entitlements')
end

project.save
puts 'Entitlements wired'