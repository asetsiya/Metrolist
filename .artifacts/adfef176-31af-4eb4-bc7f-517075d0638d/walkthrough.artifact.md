# Walkthrough - Fix Unresolved Reference 'PingPayload'

I have fixed the "Unresolved reference 'PingPayload'" build error by adding the missing message definitions to the Protobuf schema and updating the application's protocol handling logic.

## Changes Made

### Protobuf Schema
- Updated [listentogether.proto](file:///home/dux/Room/0_active/fork/Metrolist/metroproto/listentogether.proto) to include `PingPayload` and `PongPayload` message definitions. These messages are used for RTT (Round-trip time) measurements between the client and server.

### Application Logic
- Added `PingPayload` and `PongPayload` data classes to [Protocol.kt](file:///home/dux/Room/0_active/fork/Metrolist/app/src/main/kotlin/com/metrolist/music/listentogether/Protocol.kt) to match the Protobuf definitions.
- Updated [MessageCodec.kt](file:///home/dux/Room/0_active/fork/Metrolist/app/src/main/kotlin/com/metrolist/music/listentogether/MessageCodec.kt) to support encoding `PingPayload` and decoding `PongPayload`.

## Verification Results

### Automated Tests
- Successfully ran `./gradlew :app:generateProto` to regenerate the Java and Kotlin Protobuf classes.
- Successfully compiled the project using `./gradlew :app:compileGmsReleaseKotlin`.
- Verified the fix by running a full build: `./gradlew :app:assembleFossDebug`.

> [!NOTE]
> The build error was caused by a mismatch between the checked-in Kotlin Protobuf DSL files (which expected these payloads) and the `.proto` schema (which lacked them). Synchronizing the schema and updating the codec resolved the issue.
