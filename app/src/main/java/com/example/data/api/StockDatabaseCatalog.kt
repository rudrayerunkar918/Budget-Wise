package com.example.data.api

data class StockCatalogItem(
  val symbol: String, // e.g. "RELIANCE.NS" or "500325.BO"
  val displaySymbol: String, // e.g. "RELIANCE"
  val name: String, // e.g. "Reliance Industries Ltd"
  val exchange: String, // "NSE", "BSE", "NASDAQ", "NYSE", "CRYPTO", "IPO"
  val sector: String,
  val approximatePrice: Double,
  val currencySymbol: String = "₹",
  val isIpo: Boolean = false,
  val listingDate: String = "",
  val issuePrice: Double = 0.0
)

object StockDatabaseCatalog {

  val baseStocks: List<StockCatalogItem> = listOf(
    // =========================================================================
    // 1. TOP NSE BLUE-CHIP & NIFTY 50 EQUITIES (.NS)
    // =========================================================================
    StockCatalogItem("RELIANCE.NS", "RELIANCE", "Reliance Industries Ltd", "NSE", "Energy & Conglomerate", 3012.45, "₹"),
    StockCatalogItem("TCS.NS", "TCS", "Tata Consultancy Services Ltd", "NSE", "IT Services & Consulting", 4215.30, "₹"),
    StockCatalogItem("HDFCBANK.NS", "HDFCBANK", "HDFC Bank Ltd", "NSE", "Private Banking", 1654.80, "₹"),
    StockCatalogItem("INFY.NS", "INFY", "Infosys Limited", "NSE", "IT Services & Cloud", 1540.25, "₹"),
    StockCatalogItem("ICICIBANK.NS", "ICICIBANK", "ICICI Bank Ltd", "NSE", "Private Banking", 1120.60, "₹"),
    StockCatalogItem("BHARTIARTL.NS", "BHARTIARTL", "Bharti Airtel Ltd", "NSE", "Telecommunications", 1310.50, "₹"),
    StockCatalogItem("SBIN.NS", "SBIN", "State Bank of India", "NSE", "Public Sector Banking", 815.70, "₹"),
    StockCatalogItem("ITC.NS", "ITC", "ITC Limited", "NSE", "FMCG, Cigarettes & Hotels", 432.15, "₹"),
    StockCatalogItem("LT.NS", "LT", "Larsen & Toubro Ltd", "NSE", "Infrastructure & Engineering", 3540.00, "₹"),
    StockCatalogItem("HINDUNILVR.NS", "HINDUNILVR", "Hindustan Unilever Ltd", "NSE", "FMCG & Consumer Goods", 2480.20, "₹"),
    StockCatalogItem("BAJFINANCE.NS", "BAJFINANCE", "Bajaj Finance Ltd", "NSE", "NBFC & Consumer Lending", 6950.00, "₹"),
    StockCatalogItem("HCLTECH.NS", "HCLTECH", "HCL Technologies Ltd", "NSE", "IT Services & Cloud", 1520.40, "₹"),
    StockCatalogItem("KOTAKBANK.NS", "KOTAKBANK", "Kotak Mahindra Bank", "NSE", "Private Banking", 1780.00, "₹"),
    StockCatalogItem("SUNPHARMA.NS", "SUNPHARMA", "Sun Pharmaceutical Industries", "NSE", "Pharmaceuticals & Generics", 1620.00, "₹"),
    StockCatalogItem("M&M.NS", "M&M", "Mahindra & Mahindra Ltd", "NSE", "Automotive & Farm Equipment", 2850.00, "₹"),
    StockCatalogItem("MARUTI.NS", "MARUTI", "Maruti Suzuki India Ltd", "NSE", "Passenger Automobiles", 12450.00, "₹"),
    StockCatalogItem("NTPC.NS", "NTPC", "NTPC Limited", "NSE", "Power Generation & Utilities", 395.40, "₹"),
    StockCatalogItem("TATAMOTORS.NS", "TATAMOTORS", "Tata Motors Limited", "NSE", "Automotive, Trucks & EV", 441.50, "₹"),
    StockCatalogItem("ONGC.NS", "ONGC", "Oil and Natural Gas Corporation", "NSE", "Oil & Gas Exploration", 298.50, "₹"),
    StockCatalogItem("POWERGRID.NS", "POWERGRID", "Power Grid Corp of India", "NSE", "Power Transmission", 325.00, "₹"),
    StockCatalogItem("AXISBANK.NS", "AXISBANK", "Axis Bank Ltd", "NSE", "Private Banking", 1165.00, "₹"),
    StockCatalogItem("ULTRACEMCO.NS", "ULTRACEMCO", "UltraTech Cement Ltd", "NSE", "Building Materials & Cement", 11240.00, "₹"),
    StockCatalogItem("COALINDIA.NS", "COALINDIA", "Coal India Ltd", "NSE", "Energy & Mining", 480.50, "₹"),
    StockCatalogItem("ADANIENT.NS", "ADANIENT", "Adani Enterprises Ltd", "NSE", "Infrastructure & Trade", 3120.00, "₹"),
    StockCatalogItem("BAJAJFINSV.NS", "BAJAJFINSV", "Bajaj Finserv Ltd", "NSE", "Financial Services & Insurance", 1610.00, "₹"),
    StockCatalogItem("TITAN.NS", "TITAN", "Titan Company Ltd", "NSE", "Consumer, Jewelry & Watches", 3410.50, "₹"),
    StockCatalogItem("NESTLEIND.NS", "NESTLEIND", "Nestle India Ltd", "NSE", "Food & Nutrition", 2520.00, "₹"),
    StockCatalogItem("TATASTEEL.NS", "TATASTEEL", "Tata Steel Ltd", "NSE", "Steel & Metals", 154.30, "₹"),
    StockCatalogItem("JSWSTEEL.NS", "JSWSTEEL", "JSW Steel Ltd", "NSE", "Metals & Steel Manufacturing", 920.00, "₹"),
    StockCatalogItem("ASIANPAINT.NS", "ASIANPAINT", "Asian Paints Ltd", "NSE", "Paints & Coatings", 2980.40, "₹"),
    StockCatalogItem("ADANIPORTS.NS", "ADANIPORTS", "Adani Ports and SEZ", "NSE", "Ports & Logistics", 1430.00, "₹"),
    StockCatalogItem("TRENT.NS", "TRENT", "Trent Limited (Zudio, Westside)", "NSE", "Retail & Apparel", 6950.00, "₹"),
    StockCatalogItem("BEL.NS", "BEL", "Bharat Electronics Ltd", "NSE", "Defense & Aerospace", 285.00, "₹"),
    StockCatalogItem("SIEMENS.NS", "SIEMENS", "Siemens Limited", "NSE", "Industrial Engineering & Power", 6780.00, "₹"),
    StockCatalogItem("GRASIM.NS", "GRASIM", "Grasim Industries Ltd", "NSE", "Viscose & Paints", 2640.00, "₹"),
    StockCatalogItem("TECHM.NS", "TECHM", "Tech Mahindra Ltd", "NSE", "IT Services & Telecom Solutions", 1490.00, "₹"),
    StockCatalogItem("HINDALCO.NS", "HINDALCO", "Hindalco Industries Ltd", "NSE", "Aluminum & Copper", 670.00, "₹"),
    StockCatalogItem("CIPLA.NS", "CIPLA", "Cipla Limited", "NSE", "Pharmaceuticals & Respiratory", 1510.00, "₹"),
    StockCatalogItem("INDUSINDBK.NS", "INDUSINDBK", "IndusInd Bank Ltd", "NSE", "Private Banking", 1420.00, "₹"),
    StockCatalogItem("DRREDDY.NS", "DRREDDY", "Dr. Reddy's Laboratories", "NSE", "Pharmaceuticals & Generics", 6450.00, "₹"),
    StockCatalogItem("APOLLOHOSP.NS", "APOLLOHOSP", "Apollo Hospitals Enterprise", "NSE", "Healthcare & Pharmacy", 6850.00, "₹"),
    StockCatalogItem("EICHERMOT.NS", "EICHERMOT", "Eicher Motors (Royal Enfield)", "NSE", "Motorcycles & Commercials", 4650.00, "₹"),
    StockCatalogItem("WIPRO.NS", "WIPRO", "Wipro Limited", "NSE", "IT Services & Consulting", 495.80, "₹"),
    StockCatalogItem("TATACONSUM.NS", "TATACONSUM", "Tata Consumer Products", "NSE", "FMCG, Tea & Salt", 1140.00, "₹"),
    StockCatalogItem("HEROMOTOCO.NS", "HEROMOTOCO", "Hero MotoCorp Ltd", "NSE", "2-Wheeler Automobiles", 5120.00, "₹"),
    StockCatalogItem("BRITANNIA.NS", "BRITANNIA", "Britannia Industries Ltd", "NSE", "Bakery & Biscuits", 5780.00, "₹"),
    StockCatalogItem("DIVISLAB.NS", "DIVISLAB", "Divi's Laboratories Ltd", "NSE", "Active Pharmaceutical API", 4820.00, "₹"),
    StockCatalogItem("SBILIFE.NS", "SBILIFE", "SBI Life Insurance Co Ltd", "NSE", "Life Insurance", 1720.00, "₹"),
    StockCatalogItem("HDFCLIFE.NS", "HDFCLIFE", "HDFC Life Insurance Co Ltd", "NSE", "Life Insurance", 695.00, "₹"),
    StockCatalogItem("LTIM.NS", "LTIM", "LTIMindtree Limited", "NSE", "IT Consulting & Digital", 5640.00, "₹"),
    StockCatalogItem("SHRIRAMFIN.NS", "SHRIRAMFIN", "Shriram Finance Ltd", "NSE", "Commercial Vehicle Finance", 2980.00, "₹"),
    StockCatalogItem("BPCL.NS", "BPCL", "Bharat Petroleum Corp Ltd", "NSE", "Refining & Marketing", 345.00, "₹"),

    // =========================================================================
    // 2. PROMINENT NSE MID-CAPS, PSU & HIGH GROWTH EQUITIES (.NS)
    // =========================================================================
    StockCatalogItem("HAL.NS", "HAL", "Hindustan Aeronautics Ltd", "NSE", "Defense & Aerospace", 4680.00, "₹"),
    StockCatalogItem("VBL.NS", "VBL", "Varun Beverages Ltd", "NSE", "Beverages & Pepsi Bottler", 625.00, "₹"),
    StockCatalogItem("ZOMATO.NS", "ZOMATO", "Zomato Limited (Blinkit)", "NSE", "Food Delivery & Quick Commerce", 265.50, "₹"),
    StockCatalogItem("JIOFIN.NS", "JIOFIN", "Jio Financial Services Ltd", "NSE", "Fintech & Digital Lending", 340.00, "₹"),
    StockCatalogItem("DLF.NS", "DLF", "DLF Limited", "NSE", "Real Estate Development", 860.00, "₹"),
    StockCatalogItem("CHOLAFIN.NS", "CHOLAFIN", "Cholamandalam Investment", "NSE", "Vehicle & Home Finance", 1480.00, "₹"),
    StockCatalogItem("VEDL.NS", "VEDL", "Vedanta Limited", "NSE", "Diversified Natural Resources", 460.00, "₹"),
    StockCatalogItem("AMBUJACEM.NS", "AMBUJACEM", "Ambuja Cements Ltd", "NSE", "Cement & Building Materials", 610.00, "₹"),
    StockCatalogItem("GAIL.NS", "GAIL", "GAIL (India) Limited", "NSE", "Natural Gas Transmission", 225.00, "₹"),
    StockCatalogItem("BANKBARODA.NS", "BANKBARODA", "Bank of Baroda", "NSE", "Public Sector Banking", 245.00, "₹"),
    StockCatalogItem("PNB.NS", "PNB", "Punjab National Bank", "NSE", "Public Sector Banking", 112.00, "₹"),
    StockCatalogItem("CANBK.NS", "CANBK", "Canara Bank", "NSE", "Public Sector Banking", 108.00, "₹"),
    StockCatalogItem("UNIONBANK.NS", "UNIONBANK", "Union Bank of India", "NSE", "Public Sector Banking", 124.00, "₹"),
    StockCatalogItem("IOC.NS", "IOC", "Indian Oil Corporation Ltd", "NSE", "Oil Refining & Petrol", 168.00, "₹"),
    StockCatalogItem("RECLTD.NS", "RECLTD", "REC Limited", "NSE", "Power Sector Financing", 540.00, "₹"),
    StockCatalogItem("PFC.NS", "PFC", "Power Finance Corporation", "NSE", "Power Infrastructure Finance", 480.00, "₹"),
    StockCatalogItem("IRFC.NS", "IRFC", "Indian Railway Finance Corp", "NSE", "Railway Infrastructure Financing", 162.00, "₹"),
    StockCatalogItem("IRCTC.NS", "IRCTC", "Indian Railway Catering & Tourism", "NSE", "Rail Ticketing & Hospitality", 880.00, "₹"),
    StockCatalogItem("RVNL.NS", "RVNL", "Rail Vikas Nigam Ltd", "NSE", "Railway Construction & EPC", 510.00, "₹"),
    StockCatalogItem("BHEL.NS", "BHEL", "Bharat Heavy Electricals Ltd", "NSE", "Heavy Engineering & Turbines", 280.00, "₹"),
    StockCatalogItem("SUZLON.NS", "SUZLON", "Suzlon Energy Ltd", "NSE", "Wind Energy Equipment", 76.50, "₹"),
    StockCatalogItem("TATAPOWER.NS", "TATAPOWER", "Tata Power Company Ltd", "NSE", "Renewable & Thermal Power", 435.00, "₹"),
    StockCatalogItem("CDSL.NS", "CDSL", "Central Depository Services", "NSE", "Capital Market Infrastructure", 1450.00, "₹"),
    StockCatalogItem("BSE.NS", "BSE", "BSE Limited (Exchange)", "NSE", "Stock Exchange & Indices", 2880.00, "₹"),
    StockCatalogItem("ANGELONE.NS", "ANGELONE", "Angel One Ltd", "NSE", "Fintech & Stock Broking", 2650.00, "₹"),
    StockCatalogItem("POLYCAB.NS", "POLYCAB", "Polycab India Ltd", "NSE", "Cables & Electrical Goods", 6650.00, "₹"),
    StockCatalogItem("PERSISTENT.NS", "PERSISTENT", "Persistent Systems Ltd", "NSE", "Software & Enterprise AI", 4890.00, "₹"),
    StockCatalogItem("COFORGE.NS", "COFORGE", "Coforge Limited", "NSE", "IT Solutions & Aviation Tech", 6450.00, "₹"),
    StockCatalogItem("DIXON.NS", "DIXON", "Dixon Technologies (India)", "NSE", "Electronic Manufacturing EMS", 11800.00, "₹"),
    StockCatalogItem("POLICYBZR.NS", "POLICYBZR", "PB Fintech (Policybazaar)", "NSE", "Insurance & Credit Fintech", 1680.00, "₹"),
    StockCatalogItem("PAYTM.NS", "PAYTM", "One97 Communications (Paytm)", "NSE", "Digital Payments & Soundbox", 645.00, "₹"),
    StockCatalogItem("NYKAA.NS", "NYKAA", "FSN E-Commerce Ventures (Nykaa)", "NSE", "Beauty & Fashion E-Commerce", 198.00, "₹"),
    StockCatalogItem("DELHIVERY.NS", "DELHIVERY", "Delhivery Limited", "NSE", "Express Logistics & Supply Chain", 420.00, "₹"),
    StockCatalogItem("KALYANKJIL.NS", "KALYANKJIL", "Kalyan Jewellers India", "NSE", "Jewelry & Retail", 670.00, "₹"),
    StockCatalogItem("FEDERALBNK.NS", "FEDERALBNK", "The Federal Bank Ltd", "NSE", "Private Banking", 188.00, "₹"),
    StockCatalogItem("IDFCFIRSTB.NS", "IDFCFIRSTB", "IDFC First Bank Ltd", "NSE", "Private Consumer Banking", 74.50, "₹"),
    StockCatalogItem("AUBANK.NS", "AUBANK", "AU Small Finance Bank", "NSE", "Small Finance & Banking", 640.00, "₹"),
    StockCatalogItem("YESBANK.NS", "YESBANK", "Yes Bank Limited", "NSE", "Private Commercial Banking", 21.80, "₹"),
    StockCatalogItem("JINDALSTEL.NS", "JINDALSTEL", "Jindal Steel & Power Ltd", "NSE", "Steel & Mining", 975.00, "₹"),
    StockCatalogItem("NMDC.NS", "NMDC", "NMDC Limited", "NSE", "Iron Ore Mining", 225.00, "₹"),
    StockCatalogItem("SAIL.NS", "SAIL", "Steel Authority of India Ltd", "NSE", "Public Sector Steel", 132.00, "₹"),
    StockCatalogItem("NATIONALUM.NS", "NATIONALUM", "National Aluminium Co Ltd", "NSE", "Bauxite & Aluminium", 185.00, "₹"),
    StockCatalogItem("HINDZINC.NS", "HINDZINC", "Hindustan Zinc Ltd", "NSE", "Zinc, Lead & Silver", 505.00, "₹"),
    StockCatalogItem("GODREJPROP.NS", "GODREJPROP", "Godrej Properties Ltd", "NSE", "Residential Real Estate", 2950.00, "₹"),
    StockCatalogItem("LODHA.NS", "LODHA", "Macrotech Developers (Lodha)", "NSE", "Premium Residential Realty", 1220.00, "₹"),
    StockCatalogItem("OBEROIRLTY.NS", "OBEROIRLTY", "Oberoi Realty Ltd", "NSE", "Luxury Housing & Commercial", 1780.00, "₹"),
    StockCatalogItem("PHOENIXLTD.NS", "PHOENIXLTD", "The Phoenix Mills Ltd", "NSE", "Shopping Malls & Commercial", 1680.00, "₹"),
    StockCatalogItem("PRESTIGE.NS", "PRESTIGE", "Prestige Estates Projects", "NSE", "Real Estate & Hospitality", 1760.00, "₹"),
    StockCatalogItem("KPITTECH.NS", "KPITTECH", "KPIT Technologies Ltd", "NSE", "Automotive Software & EV Tech", 1640.00, "₹"),
    StockCatalogItem("CYIENT.NS", "CYIENT", "Cyient Limited", "NSE", "Engineering & Aerospace R&D", 1920.00, "₹"),
    StockCatalogItem("CUMMINSIND.NS", "CUMMINSIND", "Cummins India Ltd", "NSE", "Diesel & Natural Gas Engines", 3780.00, "₹"),
    StockCatalogItem("ABB.NS", "ABB", "ABB India Limited", "NSE", "Robotics & Industrial Automation", 7890.00, "₹"),
    StockCatalogItem("CGPOWER.NS", "CGPOWER", "CG Power & Industrial Solutions", "NSE", "Power Equipment & Motors", 710.00, "₹"),
    StockCatalogItem("BHARATFORG.NS", "BHARATFORG", "Bharat Forge Ltd", "NSE", "Forging, Defense & Auto Components", 1520.00, "₹"),
    StockCatalogItem("ASHOKLEY.NS", "ASHOKLEY", "Ashok Leyland Ltd", "NSE", "Commercial Trucks & Buses", 230.00, "₹"),
    StockCatalogItem("TVSMOTOR.NS", "TVSMOTOR", "TVS Motor Company Ltd", "NSE", "2-Wheeler & EV Mobility", 2680.00, "₹"),
    StockCatalogItem("MRF.NS", "MRF", "MRF Limited", "NSE", "Automobile Tyres & Rubber", 138500.00, "₹"),
    StockCatalogItem("BALKRISIND.NS", "BALKRISIND", "Balkrishna Industries (BKT)", "NSE", "Off-Highway Heavy Tyres", 2980.00, "₹"),
    StockCatalogItem("VOLTAS.NS", "VOLTAS", "Voltas Limited (Tata)", "NSE", "Air Conditioners & Cooling", 1680.00, "₹"),
    StockCatalogItem("HAVELLS.NS", "HAVELLS", "Havells India Ltd", "NSE", "Home Appliances & Lighting", 1890.00, "₹"),
    StockCatalogItem("ASTRAL.NS", "ASTRAL", "Astral Limited", "NSE", "Pipes, Adhesives & Sanitary", 2040.00, "₹"),
    StockCatalogItem("PAGEIND.NS", "PAGEIND", "Page Industries Ltd (Jockey)", "NSE", "Innerwear & Athleisure", 42300.00, "₹"),
    StockCatalogItem("JUBLFOOD.NS", "JUBLFOOD", "Jubilant FoodWorks (Domino's)", "NSE", "Quick Service Restaurants", 620.00, "₹"),
    StockCatalogItem("MANKIND.NS", "MANKIND", "Mankind Pharma Ltd", "NSE", "Consumer Healthcare & Pharma", 2480.00, "₹"),
    StockCatalogItem("MAXHEALTH.NS", "MAXHEALTH", "Max Healthcare Institute", "NSE", "Hospitals & Super Specialty", 960.00, "₹"),
    StockCatalogItem("COCHINSHIP.NS", "COCHINSHIP", "Cochin Shipyard Ltd", "NSE", "Naval Vessels & Ship Repair", 1750.00, "₹"),
    StockCatalogItem("MAZDOCK.NS", "MAZDOCK", "Mazagon Dock Shipbuilders", "NSE", "Submarines & Warships", 4250.00, "₹"),
    StockCatalogItem("BDL.NS", "BDL", "Bharat Dynamics Ltd", "NSE", "Missiles & Defense Tech", 1180.00, "₹"),
    StockCatalogItem("SOLARINDS.NS", "SOLARINDS", "Solar Industries India", "NSE", "Industrial & Defense Explosives", 10200.00, "₹"),
    StockCatalogItem("KAYNES.NS", "KAYNES", "Kaynes Technology India", "NSE", "IoT & Embedded Electronics", 5200.00, "₹"),

    // =========================================================================
    // 3. TOP BSE LISTED EQUITIES (WITH BSE SCRIP CODES) (.BO)
    // =========================================================================
    StockCatalogItem("500325.BO", "RELIANCE", "Reliance Industries Ltd (BSE)", "BSE", "Energy & Conglomerate", 3010.50, "₹"),
    StockCatalogItem("532540.BO", "TCS", "Tata Consultancy Services (BSE)", "BSE", "IT Services & Software", 4212.00, "₹"),
    StockCatalogItem("500180.BO", "HDFCBANK", "HDFC Bank Ltd (BSE)", "BSE", "Private Banking", 1653.20, "₹"),
    StockCatalogItem("500209.BO", "INFY", "Infosys Limited (BSE)", "BSE", "IT Services & Cloud", 1539.80, "₹"),
    StockCatalogItem("532174.BO", "ICICIBANK", "ICICI Bank Ltd (BSE)", "BSE", "Private Banking", 1120.00, "₹"),
    StockCatalogItem("532454.BO", "BHARTIARTL", "Bharti Airtel Ltd (BSE)", "BSE", "Telecommunications", 1309.80, "₹"),
    StockCatalogItem("500112.BO", "SBIN", "State Bank of India (BSE)", "BSE", "Public Sector Banking", 815.20, "₹"),
    StockCatalogItem("500875.BO", "ITC", "ITC Limited (BSE)", "BSE", "FMCG & Cigarettes", 432.00, "₹"),
    StockCatalogItem("500510.BO", "LT", "Larsen & Toubro Ltd (BSE)", "BSE", "Engineering & EPC", 3538.00, "₹"),
    StockCatalogItem("500696.BO", "HINDUNILVR", "Hindustan Unilever (BSE)", "BSE", "FMCG & Home Care", 2479.50, "₹"),
    StockCatalogItem("500034.BO", "BAJFINANCE", "Bajaj Finance Ltd (BSE)", "BSE", "Consumer Lending", 6948.00, "₹"),
    StockCatalogItem("532281.BO", "HCLTECH", "HCL Technologies (BSE)", "BSE", "IT Services", 1519.00, "₹"),
    StockCatalogItem("500247.BO", "KOTAKBANK", "Kotak Mahindra Bank (BSE)", "BSE", "Private Banking", 1779.00, "₹"),
    StockCatalogItem("524715.BO", "SUNPHARMA", "Sun Pharmaceutical (BSE)", "BSE", "Pharma & Generics", 1619.00, "₹"),
    StockCatalogItem("500520.BO", "M&M", "Mahindra & Mahindra (BSE)", "BSE", "Auto & Tractors", 2848.00, "₹"),
    StockCatalogItem("532500.BO", "MARUTI", "Maruti Suzuki (BSE)", "BSE", "Automobiles", 12440.00, "₹"),
    StockCatalogItem("532555.BO", "NTPC", "NTPC Limited (BSE)", "BSE", "Power & Energy", 395.00, "₹"),
    StockCatalogItem("500570.BO", "TATAMOTORS", "Tata Motors Ltd (BSE)", "BSE", "Automotive & EV", 441.50, "₹"),
    StockCatalogItem("500312.BO", "ONGC", "ONGC Ltd (BSE)", "BSE", "Oil & Gas", 298.00, "₹"),
    StockCatalogItem("532898.BO", "POWERGRID", "Power Grid Corp (BSE)", "BSE", "Transmission", 324.80, "₹"),
    StockCatalogItem("532215.BO", "AXISBANK", "Axis Bank Ltd (BSE)", "BSE", "Banking", 1164.50, "₹"),
    StockCatalogItem("532538.BO", "ULTRACEMCO", "UltraTech Cement (BSE)", "BSE", "Cement", 11235.00, "₹"),
    StockCatalogItem("533278.BO", "COALINDIA", "Coal India (BSE)", "BSE", "Mining", 480.00, "₹"),
    StockCatalogItem("512599.BO", "ADANIENT", "Adani Enterprises (BSE)", "BSE", "Conglomerate", 3118.00, "₹"),
    StockCatalogItem("532978.BO", "BAJAJFINSV", "Bajaj Finserv (BSE)", "BSE", "Fintech & Insurance", 1609.00, "₹"),
    StockCatalogItem("500114.BO", "TITAN", "Titan Company (BSE)", "BSE", "Jewelry & Luxury", 3408.00, "₹"),
    StockCatalogItem("500790.BO", "NESTLEIND", "Nestle India (BSE)", "BSE", "Food Products", 2518.00, "₹"),
    StockCatalogItem("500470.BO", "TATASTEEL", "Tata Steel Ltd (BSE)", "BSE", "Steel Manufacturing", 154.10, "₹"),
    StockCatalogItem("500228.BO", "JSWSTEEL", "JSW Steel Ltd (BSE)", "BSE", "Metals & Steel", 919.50, "₹"),
    StockCatalogItem("500820.BO", "ASIANPAINT", "Asian Paints Ltd (BSE)", "BSE", "Decorative Paints", 2978.00, "₹"),
    StockCatalogItem("532921.BO", "ADANIPORTS", "Adani Ports (BSE)", "BSE", "Ports & SEZ", 1428.00, "₹"),
    StockCatalogItem("500251.BO", "TRENT", "Trent Limited (BSE)", "BSE", "Fashion Retail", 6940.00, "₹"),
    StockCatalogItem("500049.BO", "BEL", "Bharat Electronics (BSE)", "BSE", "Defense Radar & Avionics", 284.50, "₹"),
    StockCatalogItem("500550.BO", "SIEMENS", "Siemens Ltd (BSE)", "BSE", "Industrial Engineering", 6775.00, "₹"),
    StockCatalogItem("500300.BO", "GRASIM", "Grasim Industries (BSE)", "BSE", "Textiles & Cement", 2638.00, "₹"),
    StockCatalogItem("532755.BO", "TECHM", "Tech Mahindra (BSE)", "BSE", "IT & Telecom", 1488.00, "₹"),
    StockCatalogItem("500440.BO", "HINDALCO", "Hindalco Industries (BSE)", "BSE", "Metals & Aluminum", 669.00, "₹"),
    StockCatalogItem("500087.BO", "CIPLA", "Cipla Ltd (BSE)", "BSE", "Pharma Generics", 1508.00, "₹"),
    StockCatalogItem("532187.BO", "INDUSINDBK", "IndusInd Bank (BSE)", "BSE", "Banking", 1418.00, "₹"),
    StockCatalogItem("500124.BO", "DRREDDY", "Dr. Reddy's Lab (BSE)", "BSE", "Pharmaceuticals", 6445.00, "₹"),
    StockCatalogItem("508869.BO", "APOLLOHOSP", "Apollo Hospitals (BSE)", "BSE", "Healthcare", 6840.00, "₹"),
    StockCatalogItem("505200.BO", "EICHERMOT", "Eicher Motors (BSE)", "BSE", "Automotive", 4645.00, "₹"),
    StockCatalogItem("507685.BO", "WIPRO", "Wipro Limited (BSE)", "BSE", "IT Services", 495.20, "₹"),
    StockCatalogItem("541154.BO", "HAL", "Hindustan Aeronautics (BSE)", "BSE", "Defense & Fighter Jets", 4675.00, "₹"),
    StockCatalogItem("543320.BO", "ZOMATO", "Zomato Limited (BSE)", "BSE", "Food & Quick Commerce", 265.00, "₹"),
    StockCatalogItem("543940.BO", "JIOFIN", "Jio Financial Services (BSE)", "BSE", "Fintech & AMC", 339.50, "₹"),
    StockCatalogItem("532868.BO", "DLF", "DLF Limited (BSE)", "BSE", "Real Estate", 859.00, "₹"),
    StockCatalogItem("500400.BO", "TATAPOWER", "Tata Power (BSE)", "BSE", "Power & Solar", 434.50, "₹"),
    StockCatalogItem("540575.BO", "CDSL", "CDSL Depository (BSE)", "BSE", "Demat Services", 1448.00, "₹"),
    StockCatalogItem("543257.BO", "IRFC", "Indian Railway Finance (BSE)", "BSE", "Rail Infrastructure", 161.80, "₹"),
    StockCatalogItem("542830.BO", "IRCTC", "IRCTC Limited (BSE)", "BSE", "Rail Ticketing", 879.00, "₹"),
    StockCatalogItem("542649.BO", "RVNL", "Rail Vikas Nigam (BSE)", "BSE", "Rail EPC", 509.00, "₹"),
    StockCatalogItem("532667.BO", "SUZLON", "Suzlon Energy (BSE)", "BSE", "Wind Energy", 76.40, "₹"),

    // =========================================================================
    // 4. INDIAN IPOs (NEW & RECENT 2024–2026 LISTINGS)
    // =========================================================================
    StockCatalogItem("SWIGGY.NS", "SWIGGY", "Swiggy Limited (IPO)", "IPO", "Food Delivery, Instamart & Dineout", 452.00, "₹", isIpo = true, listingDate = "Nov 2024", issuePrice = 390.0),
    StockCatalogItem("544280.BO", "SWIGGY", "Swiggy Ltd (BSE IPO)", "BSE", "Quick Commerce & Tech", 451.50, "₹", isIpo = true, listingDate = "Nov 2024", issuePrice = 390.0),
    StockCatalogItem("HYUNDAI.NS", "HYUNDAI", "Hyundai Motor India Ltd (IPO)", "IPO", "Automobiles & Electric Vehicles", 1795.00, "₹", isIpo = true, listingDate = "Oct 2024", issuePrice = 1960.0),
    StockCatalogItem("544275.BO", "HYUNDAI", "Hyundai Motor India (BSE IPO)", "BSE", "Automobiles", 1794.00, "₹", isIpo = true, listingDate = "Oct 2024", issuePrice = 1960.0),
    StockCatalogItem("WAAREEENER.NS", "WAAREEENER", "Waaree Energies Ltd (IPO)", "IPO", "Solar PV Modules & Green Energy", 2840.00, "₹", isIpo = true, listingDate = "Oct 2024", issuePrice = 1503.0),
    StockCatalogItem("544277.BO", "WAAREEENER", "Waaree Energies (BSE IPO)", "BSE", "Solar Manufacturing", 2835.00, "₹", isIpo = true, listingDate = "Oct 2024", issuePrice = 1503.0),
    StockCatalogItem("PREMIERENE.NS", "PREMIERENE", "Premier Energies Ltd (IPO)", "IPO", "Solar Cells & EPC Solutions", 1120.00, "₹", isIpo = true, listingDate = "Sep 2024", issuePrice = 450.0),
    StockCatalogItem("544238.BO", "PREMIERENE", "Premier Energies (BSE IPO)", "BSE", "Solar Cells Manufacturing", 1118.00, "₹", isIpo = true, listingDate = "Sep 2024", issuePrice = 450.0),
    StockCatalogItem("BAJAJHFL.NS", "BAJAJHFL", "Bajaj Housing Finance Ltd (IPO)", "IPO", "Housing & Mortgage Finance", 132.50, "₹", isIpo = true, listingDate = "Sep 2024", issuePrice = 70.0),
    StockCatalogItem("544252.BO", "BAJAJHFL", "Bajaj Housing Finance (BSE IPO)", "BSE", "Mortgage Lending", 132.20, "₹", isIpo = true, listingDate = "Sep 2024", issuePrice = 70.0),
    StockCatalogItem("OLAELEC.NS", "OLAELEC", "Ola Electric Mobility (IPO)", "IPO", "Electric 2-Wheelers & Battery", 68.40, "₹", isIpo = true, listingDate = "Aug 2024", issuePrice = 76.0),
    StockCatalogItem("544226.BO", "OLAELEC", "Ola Electric Mobility (BSE IPO)", "BSE", "EV Manufacturing", 68.30, "₹", isIpo = true, listingDate = "Aug 2024", issuePrice = 76.0),
    StockCatalogItem("FIRSTCRY.NS", "FIRSTCRY", "Brainbees Solutions FirstCry (IPO)", "IPO", "Baby Care, Kids & Omnichannel", 575.00, "₹", isIpo = true, listingDate = "Aug 2024", issuePrice = 465.0),
    StockCatalogItem("544228.BO", "FIRSTCRY", "Brainbees Solutions (BSE IPO)", "BSE", "Retail & Baby Products", 574.00, "₹", isIpo = true, listingDate = "Aug 2024", issuePrice = 465.0),
    StockCatalogItem("DIFFUSION.NS", "DIFFUSION", "Diffusion Engineers Ltd (IPO)", "IPO", "Wear Protection & Welding", 345.00, "₹", isIpo = true, listingDate = "Oct 2024", issuePrice = 168.0),
    StockCatalogItem("NORTHARC.NS", "NORTHARC", "Northern Arc Capital Ltd (IPO)", "IPO", "Diversified Financial Services", 288.00, "₹", isIpo = true, listingDate = "Sep 2024", issuePrice = 263.0),
    StockCatalogItem("KRN.NS", "KRN", "KRN Heat Exchanger Ltd (IPO)", "IPO", "HVAC Components & Fin Coils", 480.00, "₹", isIpo = true, listingDate = "Oct 2024", issuePrice = 220.0),
    StockCatalogItem("ARKADE.NS", "ARKADE", "Arkade Developers Ltd (IPO)", "IPO", "Redevelopment Real Estate", 165.00, "₹", isIpo = true, listingDate = "Sep 2024", issuePrice = 128.0),
    StockCatalogItem("WCIL.NS", "WCIL", "Western Carriers India (IPO)", "IPO", "Multi-modal Rail Logistics", 145.00, "₹", isIpo = true, listingDate = "Sep 2024", issuePrice = 172.0),
    StockCatalogItem("SARASWATI.NS", "SARASWATI", "Saraswati Saree Dhyan (IPO)", "IPO", "Women Ethnic Apparel", 178.00, "₹", isIpo = true, listingDate = "Aug 2024", issuePrice = 160.0),
    StockCatalogItem("UNICOMM.NS", "UNICOMM", "Unicommerce eSolutions (IPO)", "IPO", "E-Commerce SaaS & Logistics Tech", 215.00, "₹", isIpo = true, listingDate = "Aug 2024", issuePrice = 108.0),
    StockCatalogItem("CEIGALL.NS", "CEIGALL", "Ceigall India Ltd (IPO)", "IPO", "Highways & Expressways EPC", 340.00, "₹", isIpo = true, listingDate = "Aug 2024", issuePrice = 401.0),
    StockCatalogItem("AKUMS.NS", "AKUMS", "Akums Drugs & Pharma (IPO)", "IPO", "CDMO & Pharmaceutical Formulations", 620.00, "₹", isIpo = true, listingDate = "Aug 2024", issuePrice = 679.0),
    StockCatalogItem("SANSTAR.NS", "SANSTAR", "Sanstar Limited (IPO)", "IPO", "Specialty Plant-based Products", 125.00, "₹", isIpo = true, listingDate = "Jul 2024", issuePrice = 95.0),
    StockCatalogItem("BANSALWIRE.NS", "BANSALWIRE", "Bansal Wire Industries (IPO)", "IPO", "Stainless Steel Wires", 365.00, "₹", isIpo = true, listingDate = "Jul 2024", issuePrice = 256.0),
    StockCatalogItem("EMCURE.NS", "EMCURE", "Emcure Pharmaceuticals (IPO)", "IPO", "Formulations & Biotechnology", 1380.00, "₹", isIpo = true, listingDate = "Jul 2024", issuePrice = 1008.0),
    StockCatalogItem("ABD.NS", "ABD", "Allied Blenders & Distillers (IPO)", "IPO", "Spirits, Alcoholic Beverages", 325.00, "₹", isIpo = true, listingDate = "Jul 2024", issuePrice = 281.0),
    StockCatalogItem("STANLEY.NS", "STANLEY", "Stanley Lifestyles Ltd (IPO)", "IPO", "Luxury Furniture & Leather", 460.00, "₹", isIpo = true, listingDate = "Jun 2024", issuePrice = 369.0),
    StockCatalogItem("IXIGO.NS", "IXIGO", "Le Travenues Tech (ixigo) (IPO)", "IPO", "Online Train & Flight Booking", 168.00, "₹", isIpo = true, listingDate = "Jun 2024", issuePrice = 93.0),
    StockCatalogItem("KRONOX.NS", "KRONOX", "Kronox Lab Sciences (IPO)", "IPO", "High Purity Specialty Chemicals", 158.00, "₹", isIpo = true, listingDate = "Jun 2024", issuePrice = 136.0),
    StockCatalogItem("AWFIS.NS", "AWFIS", "Awfis Space Solutions (IPO)", "IPO", "Flexible Co-working Workspaces", 710.00, "₹", isIpo = true, listingDate = "May 2024", issuePrice = 383.0),
    StockCatalogItem("GODIGIT.NS", "GODIGIT", "Go Digit General Insurance (IPO)", "IPO", "Digital General Insurance", 345.00, "₹", isIpo = true, listingDate = "May 2024", issuePrice = 272.0),
    StockCatalogItem("TBOTEK.NS", "TBOTEK", "TBO Tek Limited (IPO)", "IPO", "Global B2B Travel Distribution", 1560.00, "₹", isIpo = true, listingDate = "May 2024", issuePrice = 920.0),
    StockCatalogItem("AADHARHFC.NS", "AADHARHFC", "Aadhar Housing Finance (IPO)", "IPO", "Affordable Low-Income Housing", 440.00, "₹", isIpo = true, listingDate = "May 2024", issuePrice = 315.0),
    StockCatalogItem("INDEGENE.NS", "INDEGENE", "Indegene Limited (IPO)", "IPO", "Life Sciences Commercial Tech", 610.00, "₹", isIpo = true, listingDate = "May 2024", issuePrice = 452.0),
    StockCatalogItem("BHARTIHEXA.NS", "BHARTIHEXA", "Bharti Hexacom Ltd (IPO)", "IPO", "Telecommunications (Rajasthan & NE)", 1340.00, "₹", isIpo = true, listingDate = "Apr 2024", issuePrice = 570.0),
    StockCatalogItem("NTPCGREEN.NS", "NTPCGREEN", "NTPC Green Energy (Upcoming IPO)", "IPO", "Solar & Wind Clean Energy", 125.00, "₹", isIpo = true, listingDate = "Nov 2024", issuePrice = 108.0),
    StockCatalogItem("ENVIRO.NS", "ENVIRO", "Enviro Infra Engineers (IPO)", "IPO", "Water & Waste-water Solutions", 195.00, "₹", isIpo = true, listingDate = "Nov 2024", issuePrice = 148.0),
    StockCatalogItem("ZINKA.NS", "ZINKA", "Zinka Logistics (BlackBuck) (IPO)", "IPO", "Trucking Platform & Payments", 295.00, "₹", isIpo = true, listingDate = "Nov 2024", issuePrice = 273.0),

    // =========================================================================
    // 5. GLOBAL EQUITIES & CRYPTO ASSETS
    // =========================================================================
    StockCatalogItem("AAPL", "AAPL", "Apple Inc.", "NASDAQ", "Consumer Electronics & Services", 225.80, "$"),
    StockCatalogItem("MSFT", "MSFT", "Microsoft Corp.", "NASDAQ", "Software, Cloud & Azure AI", 440.50, "$"),
    StockCatalogItem("GOOGL", "GOOGL", "Alphabet Inc. (Google)", "NASDAQ", "Search, Cloud & Gemini AI", 178.40, "$"),
    StockCatalogItem("AMZN", "AMZN", "Amazon.com Inc.", "NASDAQ", "E-Commerce & AWS Cloud", 185.20, "$"),
    StockCatalogItem("NVDA", "NVDA", "NVIDIA Corporation", "NASDAQ", "Semiconductors & AI Chips", 125.60, "$"),
    StockCatalogItem("TSLA", "TSLA", "Tesla Inc.", "NASDAQ", "Electric Vehicles & Robotics", 245.30, "$"),
    StockCatalogItem("META", "META", "Meta Platforms Inc.", "NASDAQ", "Social Networks & AI", 515.20, "$"),
    StockCatalogItem("BRK-B", "BRK.B", "Berkshire Hathaway", "NYSE", "Insurance & Holdings", 450.00, "$"),
    StockCatalogItem("JPM", "JPM", "JPMorgan Chase & Co.", "NYSE", "Global Banking", 215.40, "$"),
    StockCatalogItem("V", "V", "Visa Inc.", "NYSE", "Digital Payment Rails", 270.80, "$"),
    StockCatalogItem("BTC-USD", "BTC", "Bitcoin USD", "CRYPTO", "Decentralized Digital Gold", 64500.00, "$"),
    StockCatalogItem("ETH-USD", "ETH", "Ethereum USD", "CRYPTO", "Smart Contracts & DeFi", 3450.00, "$"),
    StockCatalogItem("GC=F", "GOLD", "Gold Futures (1 Troy Oz)", "COMMODITY", "Precious Metals", 2380.00, "$")
  )

  // Dynamic additions & reactive flow
  private val dynamicIpoStocks = mutableListOf<StockCatalogItem>()
  private val dynamicCustomStocks = mutableListOf<StockCatalogItem>()
  val catalogVersion = kotlinx.coroutines.flow.MutableStateFlow(0)

  @Volatile
  private var cachedAllStocks: List<StockCatalogItem>? = null

  private const val PREFS_NAME = "cashew_stock_catalog_persistent_prefs"
  private const val KEY_CUSTOM_STOCKS = "custom_registered_stocks_json"

  fun initPersistent(context: android.content.Context) {
    try {
      val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
      val json = prefs.getString(KEY_CUSTOM_STOCKS, null)
      if (!json.isNullOrEmpty()) {
        val array = org.json.JSONArray(json)
        synchronized(this) {
          dynamicCustomStocks.clear()
          for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            dynamicCustomStocks.add(
              StockCatalogItem(
                symbol = obj.optString("symbol"),
                displaySymbol = obj.optString("displaySymbol"),
                name = obj.optString("name"),
                exchange = obj.optString("exchange", "NSE"),
                sector = obj.optString("sector", "Newly Listed Equity"),
                approximatePrice = obj.optDouble("approximatePrice", 100.0),
                currencySymbol = obj.optString("currencySymbol", "₹"),
                isIpo = obj.optBoolean("isIpo", false),
                listingDate = obj.optString("listingDate", "Listed"),
                issuePrice = obj.optDouble("issuePrice", 100.0)
              )
            )
          }
          cachedAllStocks = null
          catalogVersion.value++
        }
      }
    } catch (_: Exception) {
    }
  }

  fun getAllStocks(): List<StockCatalogItem> {
    return cachedAllStocks ?: synchronized(this) {
      cachedAllStocks ?: (baseStocks + dynamicIpoStocks + dynamicCustomStocks).also { cachedAllStocks = it }
    }
  }

  fun addDynamicIpos(newIpos: List<StockCatalogItem>) {
    var modified = false
    synchronized(this) {
      for (ipo in newIpos) {
        if (dynamicIpoStocks.none { it.symbol.equals(ipo.symbol, ignoreCase = true) } &&
            baseStocks.none { it.symbol.equals(ipo.symbol, ignoreCase = true) } &&
            dynamicCustomStocks.none { it.symbol.equals(ipo.symbol, ignoreCase = true) }) {
          dynamicIpoStocks.add(ipo)
          modified = true
        }
      }
      if (modified) {
        cachedAllStocks = null
        catalogVersion.value++
      }
    }
  }

  fun addOrUpdateStock(item: StockCatalogItem, context: android.content.Context? = null) {
    synchronized(this) {
      dynamicCustomStocks.removeAll { it.symbol.equals(item.symbol, ignoreCase = true) }
      dynamicIpoStocks.removeAll { it.symbol.equals(item.symbol, ignoreCase = true) }
      dynamicCustomStocks.add(item)
      cachedAllStocks = null
      catalogVersion.value++
    }
    if (context != null) {
      savePersistentStocks(context)
    }
  }

  fun markIpoAsListed(symbol: String, listingPrice: Double = 0.0, context: android.content.Context? = null) {
    val clean = symbol.trim().uppercase()
    synchronized(this) {
      val existing = getAllStocks().firstOrNull { it.symbol.equals(clean, ignoreCase = true) }
      val price = if (listingPrice > 0) listingPrice else (existing?.approximatePrice ?: 100.0)
      val name = existing?.name?.replace("(IPO)", "")?.replace("(Upcoming IPO)", "")?.trim() ?: clean
      val display = existing?.displaySymbol ?: clean.removeSuffix(".NS")
      val sector = existing?.sector ?: "Newly Listed NSE Equity"

      val listedItem = StockCatalogItem(
        symbol = clean,
        displaySymbol = display,
        name = "$name (Listed)",
        exchange = "NSE",
        sector = sector,
        approximatePrice = price,
        currencySymbol = "₹",
        isIpo = false,
        listingDate = "Listed on NSE",
        issuePrice = existing?.issuePrice ?: price
      )

      dynamicIpoStocks.removeAll { it.symbol.equals(clean, ignoreCase = true) }
      dynamicCustomStocks.removeAll { it.symbol.equals(clean, ignoreCase = true) }
      dynamicCustomStocks.add(listedItem)
      cachedAllStocks = null
      catalogVersion.value++
    }
    if (context != null) {
      savePersistentStocks(context)
    }
  }

  private fun savePersistentStocks(context: android.content.Context) {
    try {
      val array = org.json.JSONArray()
      synchronized(this) {
        for (item in dynamicCustomStocks) {
          val obj = org.json.JSONObject().apply {
            put("symbol", item.symbol)
            put("displaySymbol", item.displaySymbol)
            put("name", item.name)
            put("exchange", item.exchange)
            put("sector", item.sector)
            put("approximatePrice", item.approximatePrice)
            put("currencySymbol", item.currencySymbol)
            put("isIpo", item.isIpo)
            put("listingDate", item.listingDate)
            put("issuePrice", item.issuePrice)
          }
          array.put(obj)
        }
      }
      context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_CUSTOM_STOCKS, array.toString())
        .apply()
    } catch (_: Exception) {
    }
  }

  fun getDynamicIpos(): List<StockCatalogItem> = dynamicIpoStocks.toList()

  fun search(query: String, exchangeFilter: String? = null, ipoOnly: Boolean = false): List<StockCatalogItem> {
    val q = query.trim().lowercase()
    val all = getAllStocks()

    return all.filter { item ->
      val matchesExchange = when (exchangeFilter?.uppercase()) {
        null, "", "ALL" -> true
        "NSE" -> item.exchange == "NSE" || item.symbol.endsWith(".NS")
        "BSE" -> item.exchange == "BSE" || item.symbol.endsWith(".BO")
        "IPO", "IPOS" -> item.isIpo || item.exchange == "IPO"
        "GLOBAL" -> item.exchange == "NASDAQ" || item.exchange == "NYSE"
        "CRYPTO" -> item.exchange == "CRYPTO" || item.exchange == "COMMODITY"
        else -> item.exchange.equals(exchangeFilter, ignoreCase = true)
      }

      val matchesIpo = !ipoOnly || item.isIpo || item.exchange == "IPO"

      val matchesQuery = q.isEmpty() ||
        item.symbol.lowercase().contains(q) ||
        item.displaySymbol.lowercase().contains(q) ||
        item.name.lowercase().contains(q) ||
        item.sector.lowercase().contains(q)

      matchesExchange && matchesIpo && matchesQuery
    }
  }

  fun getFallbackQuote(symbol: String): StockQuote? {
    val clean = symbol.trim().uppercase()
    val all = getAllStocks()
    val match = all.firstOrNull {
      it.symbol.equals(clean, ignoreCase = true) ||
        it.displaySymbol.equals(clean, ignoreCase = true) ||
        it.symbol.startsWith("$clean.") ||
        clean.startsWith("${it.displaySymbol}.")
    } ?: return null

    val price = match.approximatePrice
    return StockQuote(
      symbol = match.symbol,
      regularMarketPrice = price,
      previousClose = price * 0.992,
      changeAmount = price * 0.008,
      changePercent = 0.8,
      currency = if (match.currencySymbol == "₹") "INR" else "USD",
      timestamp = System.currentTimeMillis(),
      exchange = match.exchange,
      displayName = match.name,
      isRealTime = false
    )
  }
}
