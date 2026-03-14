class HybridIncomingCall : HybridIncomingCallSpec {

  // UIView
  var view: UIView = UIView()

  // props
  var color: String = "#000" {
    didSet {
      view.backgroundColor = hexStringToUIColor(hexColor: color)
    }
  }
  
  func hexStringToUIColor(hexColor: String) -> UIColor {
    var hex = hexColor.trimmingCharacters(in: .whitespacesAndNewlines)
    if hex.hasPrefix("#") {
      hex = String(hex.dropFirst())
    }
    var color: UInt64 = 0
    Scanner(string: hex).scanHexInt64(&color)
    let r = CGFloat((color >> 16) & 0xFF) / 255.0
    let g = CGFloat((color >> 8) & 0xFF) / 255.0
    let b = CGFloat(color & 0xFF) / 255.0
    return UIColor(red: r, green: g, blue: b, alpha: 1)
  }  
}
