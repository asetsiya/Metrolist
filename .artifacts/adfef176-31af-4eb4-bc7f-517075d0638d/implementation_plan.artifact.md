# Fix Unresolved Reference 'PingPayload'

The project is failing to build because `PingPayloadKt.kt` and `PongPayloadKt.kt` (Kotlin Protobuf DSL files) are present in the source tree, but the corresponding message definitions are missing from `listentogether.proto`. This causes the generated Java class `Listentogether` to lack the `PingPayload` and `PongPayload` inner classes, leading to "Unresolved reference" errors during Kotlin compilation.

Based on previous implementation artifacts, these messages were intended to be added to support latency measurement (RTT) in the "Listen Together" protocol.

## Proposed Changes

### [metroproto](file:///home/dux/Room/0_active/fork/Metrolist/metroproto)

#### [MODIFY] [listentogether.proto](file:///home/dux/Room/0_active/fork/Metrolist/metroproto/listentogether.proto)
- Add `PingPayload` message with `client_time` and `sequence` fields.
- Add `PongPayload` message with `client_time`, `server_receive_time`, `server_send_time`, and `sequence` fields.

### [app](file:///home/dux/Room/0_active/fork/Metrolist/app)

#### [MODIFY] [Protocol.kt](file:///home/dux/Room/0_active/fork/Metrolist/app/src/main/kotlin/com/metrolist/music/listentogether/Protocol.kt)
- Add `PingPayload` and `PongPayload` data classes with `@Serializable` annotation to match the Protobuf definitions.

#### [MODIFY] [MessageCodec.kt](file:///home/dux/Room/0_active/fork/Metrolist/app/src/main/kotlin/com/metrolist/music/listentogether/MessageCodec.kt)
- Update `toProtoMessage` to support encoding `PingPayload`.
- Update `decodeProtobufPayload` to support decoding `PongPayload`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:generateProto` to ensure Java classes are correctly generated.
- Run `./gradlew :app:compileGmsReleaseKotlin` (the failing command) to verify the build now passes.
- Run `./gradlew :app:assembleFossDebug` as per `AGENTS.md` instructions.

### Manual Verification
- N/A (Build fix only).
