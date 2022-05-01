#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint rtmp_with_capture.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'rtmp_with_capture'
  s.version          = '0.0.1'
  s.summary          = 'A new flutter plugin to stream video to RTMP server and simultaneously capture images'
  s.description      = <<-DESC
A new flutter plugin to stream video to RTMP server and simultaneously capture images
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'potatohead12@snu.ac.kr' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.dependency 'HaishinKit', '~> 1.0.10'
  s.platform = :ios, '8.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end
