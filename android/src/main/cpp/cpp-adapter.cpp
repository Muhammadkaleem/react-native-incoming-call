#include <jni.h>
#include "incomingcallOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::incomingcall::initialize(vm);
}
