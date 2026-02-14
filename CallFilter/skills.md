# Skills — Guía técnica de implementación

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Shared (KMP) | Kotlin Multiplatform, kotlinx.coroutines, kotlinx.serialization |
| Persistencia shared | multiplatform-settings o DataStore KMP |
| Android UI | Jetpack Compose + Material 3 |
| Android Service | CallScreeningService (API 24+) |
| iOS UI | SwiftUI (placeholder) |
| Build | Gradle con version catalogs |
| Testing | kotlin.test (commonTest), JUnit (androidTest) |

## Estructura del proyecto

```
project-root/
├── shared/
│   └── src/
│       ├── commonMain/kotlin/com/callshield/
│       │   ├── model/
│       │   │   ├── Rule.kt
│       │   │   ├── RuleType.kt
│       │   │   ├── BlockedNumber.kt
│       │   │   ├── RuleResult.kt
│       │   │   └── SystemState.kt
│       │   ├── engine/
│       │   │   ├── RuleEngine.kt
│       │   │   └── RuleEvaluator.kt
│       │   ├── repository/
│       │   │   ├── RuleRepository.kt
│       │   │   ├── BlockedNumberRepository.kt
│       │   │   └── PreferencesRepository.kt
│       │   └── config/
│       │       └── AppConfig.kt
│       ├── commonTest/kotlin/com/callshield/
│       │   └── engine/
│       │       └── RuleEngineTest.kt
│       ├── androidMain/
│       └── iosMain/
├── androidApp/
│   └── src/main/
│       ├── java/com/callshield/android/
│       │   ├── service/
│       │   │   └── CallBlockerService.kt
│       │   ├── ui/
│       │   │   ├── onboarding/
│       │   │   ├── home/
│       │   │   ├── rules/
│       │   │   └── blockedlist/
│       │   ├── notification/
│       │   │   └── BlockNotificationManager.kt
│       │   └── system/
│       │       └── SystemStateChecker.kt
│       └── AndroidManifest.xml
└── iosApp/
    └── iosApp/
        ├── ContentView.swift
        └── Screens/ (placeholder)
```

## Modelos de datos (shared/commonMain)

### Rule.kt
```kotlin
data class Rule(
    val id: String,
    val type: RuleType,
    val enabled: Boolean,
    val config: RuleConfig? = null
)

sealed class RuleConfig {
    data class RegexPattern(val pattern: String) : RuleConfig()
    data class DigitCount(val count: Int) : RuleConfig()
}
```

### RuleType.kt
```kotlin
enum class RuleType {
    BLOCK_DIGIT_COUNT,    // Bloquear por cantidad de dígitos (default: 8)
    BLOCK_FROM_LIST,      // Bloquear si está en lista local
    BLOCK_REGEX,          // Bloquear por patrón regex
    BLOCK_ALL             // Bloquear todas las llamadas
}
```

### RuleResult.kt
```kotlin
enum class RuleResult {
    ALLOW,
    REJECT
}
```

### SystemState.kt
```kotlin
enum class SystemState {
    NOT_CONFIGURED,        // Rol de filtrado no habilitado
    INCOMPLETE_SETUP,      // Hay impedimentos (batería, permisos)
    PROTECTION_ACTIVE      // Filtrado operativo
}
```

### BlockedNumber.kt
```kotlin
data class BlockedNumber(
    val id: String,
    val number: String,
    val label: String? = null
)
```

## Motor de reglas (shared/commonMain)

### RuleEngine.kt — Contrato
```kotlin
class RuleEngine(
    private val ruleRepository: RuleRepository,
    private val blockedNumberRepository: BlockedNumberRepository
) {
    fun evaluate(incomingNumber: String): RuleResult {
        val activeRules = ruleRepository.getActiveRules()
        if (activeRules.isEmpty()) return RuleResult.ALLOW

        for (rule in activeRules) {
            if (matchesRule(rule, incomingNumber)) {
                return RuleResult.REJECT
            }
        }
        return RuleResult.ALLOW
    }

    private fun matchesRule(rule: Rule, number: String): Boolean {
        return when (rule.type) {
            RuleType.BLOCK_ALL -> true
            RuleType.BLOCK_DIGIT_COUNT -> {
                val count = (rule.config as? RuleConfig.DigitCount)?.count ?: 8
                number.filter { it.isDigit() }.length == count
            }
            RuleType.BLOCK_FROM_LIST ->
                blockedNumberRepository.contains(number)
            RuleType.BLOCK_REGEX -> {
                val pattern = (rule.config as? RuleConfig.RegexPattern)?.pattern
                pattern != null && Regex(pattern).containsMatchIn(number)
            }
        }
    }
}
```

**Lógica de evaluación**: se recorren las reglas activas en orden. Si cualquiera coincide → REJECT. Si ninguna → ALLOW.

## Android — CallScreeningService

### CallBlockerService.kt — Patrón
```kotlin
class CallBlockerService : CallScreeningService() {

    private val ruleEngine: RuleEngine // inyectado o instanciado

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: ""
        val result = ruleEngine.evaluate(number)

        val response = CallResponse.Builder()
        if (result == RuleResult.REJECT) {
            response.setDisallowCall(true)
            response.setRejectCall(true)
            response.setSkipCallLog(false)
            response.setSkipNotification(false)
            // Notificación opcional según preferencia
        }
        respondToCall(callDetails, response.build())
    }
}
```

### AndroidManifest.xml — Declaración del servicio
```xml
<service
    android:name=".service.CallBlockerService"
    android:permission="android.permission.BIND_SCREENING_SERVICE">
    <intent-filter>
        <action android:name="android.telecom.CallScreeningService" />
    </intent-filter>
</service>
```

### Habilitación del rol de filtrado
El onboarding debe guiar al usuario para establecer la app como Call Screening provider del sistema:
```kotlin
val roleManager = getSystemService(RoleManager::class.java)
if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
    if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        startActivityForResult(intent, REQUEST_CODE_SCREENING_ROLE)
    }
}
```

## Android — Verificación de estado del sistema

### SystemStateChecker.kt — Patrón
```kotlin
class SystemStateChecker(private val context: Context) {

    fun getState(): SystemState {
        val roleManager = context.getSystemService(RoleManager::class.java)

        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            return SystemState.NOT_CONFIGURED
        }

        val powerManager = context.getSystemService(PowerManager::class.java)
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            return SystemState.INCOMPLETE_SETUP
        }

        return SystemState.PROTECTION_ACTIVE
    }
}
```

## Android — Notificaciones

Solo se muestra notificación cuando una llamada es bloqueada **y** la preferencia de notificación está activa. El `PreferencesRepository` del shared expone este flag.

```kotlin
class BlockNotificationManager(private val context: Context) {
    fun showBlockedCallNotification(number: String) {
        // Canal de notificación dedicado
        // Contenido: "Llamada bloqueada: {number}"
        // Sin acciones adicionales en MVP
    }
}
```

## iOS — Placeholder

El target iOS debe:
1. Compilar sin errores con el shared KMP.
2. Mostrar las mismas pantallas (Onboarding, Home, Rules, BlockedList) con UI básica SwiftUI.
3. Consumir el estado compartido (`SystemState`, lista de reglas, lista bloqueada).
4. **No implementar bloqueo real**. El Home puede mostrar un mensaje indicando que el bloqueo no está disponible en esta plataforma aún.

## Persistencia

Usar `multiplatform-settings` o DataStore KMP en el módulo shared para:
- Lista de reglas y su estado (activa/inactiva).
- Configuración de cada regla (regex, cantidad de dígitos).
- Lista bloqueada de números.
- Preferencias (notificaciones activas, etc.).

Todo se serializa con `kotlinx.serialization` y se almacena como key-value local.

## Testing

### Motor de reglas — Tests obligatorios (commonTest)
- Número de 8 dígitos con regla activa → REJECT.
- Número de 8 dígitos con regla inactiva → ALLOW.
- Número en lista bloqueada → REJECT.
- Número que coincide con regex → REJECT.
- BLOCK_ALL activo → REJECT para cualquier número.
- Sin reglas activas → ALLOW.
- Combinación de reglas: una coincide → REJECT.
- Regex inválido no causa crash.

## Qué NO implementar

- Historial de llamadas bloqueadas.
- Identificación de spam online.
- Sincronización en la nube.
- Estadísticas de bloqueo.
- Bloqueo basado en contactos.
- Cualquier llamada de red.
- Permisos de contactos, internet (más allá de lo que el sistema requiera).
