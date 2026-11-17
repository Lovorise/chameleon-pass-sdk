// Ergänzung in ChameleonPassPayload:
fun ChameleonPassPayload.copyWithStatus(newStatus: PassStatus): ChameleonPassPayload =
    ChameleonPassPayload(
        schemaVersion = this.schemaVersion,
        preset = this.preset,
        status = newStatus,
        theme = this.theme,
        layout = this.layout,
        header = this.header,
        holder = this.holder,
        wayfinding = this.wayfinding,
        opticalToken = this.opticalToken,
        modes = this.modes,
        actions = this.actions,
        verificationBridge = this.verificationBridge
    )

// Vollständige Config paritätisch zu Web ChameleonPassConfig:
data class ChameleonPassConfig(
    val preset: PassPreset,
    val locale: String? = null,
    val labels: com.mtchship.chameleon.core.i18n.PassUiLabelOverrides? = null,
    val colorScheme: ColorSchemeMode = ColorSchemeMode.AUTO,
    val storage: ClientStoragePolicy = ClientStoragePolicy.None,
    val transport: ChameleonTransport? = null,
    val passes: List<ChameleonPassPayload>,
    val keepScreenAwake: Boolean = true,
    val onAction: ((actionId: String, passIndex: Int) -> Unit)? = null,
    val onForward: ((passIndex: Int) -> Unit)? = null,
    val onDelegate: ((passIndex: Int) -> Unit)? = null,
    val onWalletExport: ((platform: WalletPlatform, passIndex: Int) -> Unit)? = null,
    val onComplete: ((receipt: ChameleonReceipt) -> Unit)? = null,
    val onClose: (() -> Unit)? = null
)
