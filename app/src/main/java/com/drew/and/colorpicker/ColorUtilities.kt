package com.drew.and.colorpicker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import java.io.ByteArrayOutputStream
import java.math.RoundingMode
import java.nio.ByteBuffer
import kotlin.math.pow
import kotlin.math.sqrt

var emaRed = Color.Transparent.red.toDouble()
var emaGreen = Color.Transparent.green.toDouble()
var emaBlue = Color.Transparent.blue.toDouble()
var initialized = false

val predefinedColors2 = mapOf(
    // CSS Named Colors
    "AliceBlue" to intArrayOf(240, 248, 255),
    "AntiqueWhite" to intArrayOf(250, 235, 215),
    "Aqua" to intArrayOf(0, 255, 255),
    "Aquamarine" to intArrayOf(127, 255, 212),
    "Azure" to intArrayOf(240, 255, 255),
    "Beige" to intArrayOf(245, 245, 220),
    "Bisque" to intArrayOf(255, 228, 196),
    "Black" to intArrayOf(0, 0, 0),
    "BlanchedAlmond" to intArrayOf(255, 235, 205),
    "Blue" to intArrayOf(0, 0, 255),
    "BlueViolet" to intArrayOf(138, 43, 226),
    "Brown" to intArrayOf(165, 42, 42),
    "BurlyWood" to intArrayOf(222, 184, 135),
    "CadetBlue" to intArrayOf(95, 158, 160),
    "Chartreuse" to intArrayOf(127, 255, 0),
    "Chocolate" to intArrayOf(210, 105, 30),
    "Coral" to intArrayOf(255, 127, 80),
    "CornflowerBlue" to intArrayOf(100, 149, 237),
    "Cornsilk" to intArrayOf(255, 248, 220),
    "Crimson" to intArrayOf(220, 20, 60),
    "Cyan" to intArrayOf(0, 255, 255),
    "DarkBlue" to intArrayOf(0, 0, 139),
    "DarkCyan" to intArrayOf(0, 139, 139),
    "DarkGoldenRod" to intArrayOf(184, 134, 11),
    "DarkGray" to intArrayOf(169, 169, 169),
    "DarkGreen" to intArrayOf(0, 100, 0),
    "DarkKhaki" to intArrayOf(189, 183, 107),
    "DarkMagenta" to intArrayOf(139, 0, 139),
    "DarkOliveGreen" to intArrayOf(85, 107, 47),
    "DarkOrange" to intArrayOf(255, 140, 0),
    "DarkOrchid" to intArrayOf(153, 50, 204),
    "DarkRed" to intArrayOf(139, 0, 0),
    "DarkSalmon" to intArrayOf(233, 150, 122),
    "DarkSeaGreen" to intArrayOf(143, 188, 143),
    "DarkSlateBlue" to intArrayOf(72, 61, 139),
    "DarkSlateGray" to intArrayOf(47, 79, 79),
    "DarkTurquoise" to intArrayOf(0, 206, 209),
    "DarkViolet" to intArrayOf(148, 0, 211),
    "DeepPink" to intArrayOf(255, 20, 147),
    "DeepSkyBlue" to intArrayOf(0, 191, 255),
    "DimGray" to intArrayOf(105, 105, 105),
    "DodgerBlue" to intArrayOf(30, 144, 255),
    "FireBrick" to intArrayOf(178, 34, 34),
    "FloralWhite" to intArrayOf(255, 250, 240),
    "ForestGreen" to intArrayOf(34, 139, 34),
    "Fuchsia" to intArrayOf(255, 0, 255),
    "Gainsboro" to intArrayOf(220, 220, 220),
    "GhostWhite" to intArrayOf(248, 248, 255),
    "Gold" to intArrayOf(255, 215, 0),
    "GoldenRod" to intArrayOf(218, 165, 32),
    "Gray" to intArrayOf(128, 128, 128),
    "Green" to intArrayOf(0, 128, 0),
    "GreenYellow" to intArrayOf(173, 255, 47),
    "HoneyDew" to intArrayOf(240, 255, 240),
    "HotPink" to intArrayOf(255, 105, 180),
    "IndianRed" to intArrayOf(205, 92, 92),
    "Indigo" to intArrayOf(75, 0, 130),
    "Ivory" to intArrayOf(255, 255, 240),
    "Khaki" to intArrayOf(240, 230, 140),
    "Lavender" to intArrayOf(230, 230, 250),
    "LavenderBlush" to intArrayOf(255, 240, 245),
    "LawnGreen" to intArrayOf(124, 252, 0),
    "LemonChiffon" to intArrayOf(255, 250, 205),
    "LightBlue" to intArrayOf(173, 216, 230),
    "LightCoral" to intArrayOf(240, 128, 128),
    "LightCyan" to intArrayOf(224, 255, 255),
    "LightGoldenRodYellow" to intArrayOf(250, 250, 210),
    "LightGray" to intArrayOf(211, 211, 211),
    "LightGreen" to intArrayOf(144, 238, 144),
    "LightPink" to intArrayOf(255, 182, 193),
    "LightSalmon" to intArrayOf(255, 160, 122),
    "LightSeaGreen" to intArrayOf(32, 178, 170),
    "LightSkyBlue" to intArrayOf(135, 206, 250),
    "LightSlateGray" to intArrayOf(119, 136, 153),
    "LightSteelBlue" to intArrayOf(176, 196, 222),
    "LightYellow" to intArrayOf(255, 255, 224),
    "Lime" to intArrayOf(0, 255, 0),
    "LimeGreen" to intArrayOf(50, 205, 50),
    "Linen" to intArrayOf(250, 240, 230),
    "Magenta" to intArrayOf(255, 0, 255),
    "Maroon" to intArrayOf(128, 0, 0),
    "MediumAquaMarine" to intArrayOf(102, 205, 170),
    "MediumBlue" to intArrayOf(0, 0, 205),
    "MediumOrchid" to intArrayOf(186, 85, 211),
    "MediumPurple" to intArrayOf(147, 112, 219),
    "MediumSeaGreen" to intArrayOf(60, 179, 113),
    "MediumSlateBlue" to intArrayOf(123, 104, 238),
    "MediumSpringGreen" to intArrayOf(0, 250, 154),
    "MediumTurquoise" to intArrayOf(72, 209, 204),
    "MediumVioletRed" to intArrayOf(199, 21, 133),
    "MidnightBlue" to intArrayOf(25, 25, 112),
    "MintCream" to intArrayOf(245, 255, 250),
    "MistyRose" to intArrayOf(255, 228, 225),
    "Moccasin" to intArrayOf(255, 228, 181),
    "NavajoWhite" to intArrayOf(255, 222, 173),
    "Navy" to intArrayOf(0, 0, 128),
    "OldLace" to intArrayOf(253, 245, 230),
    "Olive" to intArrayOf(128, 128, 0),
    "OliveDrab" to intArrayOf(107, 142, 35),
    "Orange" to intArrayOf(255, 165, 0),
    "OrangeRed" to intArrayOf(255, 69, 0),
    "Orchid" to intArrayOf(218, 112, 214),
    "PaleGoldenRod" to intArrayOf(238, 232, 170),
    "PaleGreen" to intArrayOf(152, 251, 152),
    "PaleTurquoise" to intArrayOf(175, 238, 238),
    "PaleVioletRed" to intArrayOf(219, 112, 147),
    "PapayaWhip" to intArrayOf(255, 239, 213),
    "PeachPuff" to intArrayOf(255, 218, 185),
    "Peru" to intArrayOf(205, 133, 63),
    "Pink" to intArrayOf(255, 192, 203),
    "Plum" to intArrayOf(221, 160, 221),
    "PowderBlue" to intArrayOf(176, 224, 230),
    "Purple" to intArrayOf(128, 0, 128),
    "RebeccaPurple" to intArrayOf(102, 51, 153),
    "Red" to intArrayOf(255, 0, 0),
    "RosyBrown" to intArrayOf(188, 143, 143),
    "RoyalBlue" to intArrayOf(65, 105, 225),
    "SaddleBrown" to intArrayOf(139, 69, 19),
    "Salmon" to intArrayOf(250, 128, 114),
    "SandyBrown" to intArrayOf(244, 164, 96),
    "SeaGreen" to intArrayOf(46, 139, 87),
    "SeaShell" to intArrayOf(255, 245, 238),
    "Sienna" to intArrayOf(160, 82, 45),
    "Silver" to intArrayOf(192, 192, 192),
    "SkyBlue" to intArrayOf(135, 206, 235),
    "SlateBlue" to intArrayOf(106, 90, 205),
    "SlateGray" to intArrayOf(112, 128, 144),
    "Snow" to intArrayOf(255, 250, 250),
    "SpringGreen" to intArrayOf(0, 255, 127),
    "SteelBlue" to intArrayOf(70, 130, 180),
    "Tan" to intArrayOf(210, 180, 140),
    "Teal" to intArrayOf(0, 128, 128),
    "Thistle" to intArrayOf(216, 191, 216),
    "Tomato" to intArrayOf(255, 99, 71),
    "Turquoise" to intArrayOf(64, 224, 208),
    "Violet" to intArrayOf(238, 130, 238),
    "Wheat" to intArrayOf(245, 222, 179),
    "White" to intArrayOf(255, 255, 255),
    "WhiteSmoke" to intArrayOf(245, 245, 245),
    "Yellow" to intArrayOf(255, 255, 0),
    "YellowGreen" to intArrayOf(154, 205, 50),

    // Additional X11 Colors
    "X11Maroon" to intArrayOf(176, 48, 96),
    "X11BrightRed" to intArrayOf(255, 0, 0),
    "X11BrickRed" to intArrayOf(203, 65, 84),
    "X11Orange" to intArrayOf(255, 165, 0),
    "X11Olive" to intArrayOf(107, 142, 35),
    "X11LightGreen" to intArrayOf(144, 238, 144),
    "X11DarkGreen" to intArrayOf(0, 100, 0),
    "X11MidnightBlue" to intArrayOf(25, 25, 112),
    "X11NavyBlue" to intArrayOf(0, 0, 128),
    "X11DarkBlue" to intArrayOf(0, 0, 139),
    "X11Cyan" to intArrayOf(0, 255, 255),
    "X11Teal" to intArrayOf(0, 128, 128),
    "X11SlateGray" to intArrayOf(112, 128, 144),

    // Additional Resene Colors
    "ReseneBlack" to intArrayOf(0, 0, 0),
    "ReseneWhite" to intArrayOf(255, 255, 255),
    "ReseneRed" to intArrayOf(255, 0, 0),
    "ReseneGreen" to intArrayOf(0, 128, 0),
    "ReseneBlue" to intArrayOf(0, 0, 255),
    "ReseneYellow" to intArrayOf(255, 255, 0),
    "ReseneOrange" to intArrayOf(255, 165, 0),
    "ResenePurple" to intArrayOf(128, 0, 128),
    "ResenePink" to intArrayOf(255, 192, 203),
    "ReseneBrown" to intArrayOf(165, 42, 42),
    "ReseneGray" to intArrayOf(128, 128, 128),
    "ReseneOlive" to intArrayOf(107, 142, 35),
    "ReseneSkyBlue" to intArrayOf(135, 206, 235),
    "ReseneViolet" to intArrayOf(238, 130, 238),
    "ReseneLimeGreen" to intArrayOf(50, 205, 50),
    "ReseneCrimson" to intArrayOf(220, 20, 60),
    "ReseneGold" to intArrayOf(255, 215, 0),
    "ReseneMint" to intArrayOf(245, 255, 250)
)

val predefinedColors3 = mapOf(
    "Absolute Zero" to intArrayOf(0, 28, 73),
    "Acid Green" to intArrayOf(69, 75, 10),
    "Aero" to intArrayOf(49, 73, 91),
    "African Violet" to intArrayOf(70, 52, 75),
    "Air Superiority Blue" to intArrayOf(45, 63, 76),
    "Alice Blue" to intArrayOf(94, 97, 100),
    "Alizarin" to intArrayOf(86, 18, 26),
    "Alloy Orange" to intArrayOf(77, 38, 6),
    "Almond" to intArrayOf(93, 85, 77),
    "Amaranth Deep Purple" to intArrayOf(62, 17, 41),
    "Amaranth Pink" to intArrayOf(95, 61, 73),
    "Amaranth Purple" to intArrayOf(67, 15, 31),
    "Amazon" to intArrayOf(23, 48, 34),
    "Amber" to intArrayOf(100, 75, 0),
    "Amethyst" to intArrayOf(60, 40, 80),
    "Android Green" to intArrayOf(24, 86, 53),
    "Antique Brass" to intArrayOf(78, 54, 40),
    "Antique Bronze" to intArrayOf(40, 36, 12),
    "Antique Fuchsia" to intArrayOf(57, 36, 51),
    "Antique Ruby" to intArrayOf(52, 11, 18),
    "Antique White" to intArrayOf(98, 92, 84),
    "Apricot" to intArrayOf(98, 81, 69),
    "Aqua" to intArrayOf(0, 100, 100),
    "Aquamarine" to intArrayOf(50, 100, 83),
    "Arctic Lime" to intArrayOf(82, 100, 8),
    "Artichoke Green" to intArrayOf(29, 44, 27),
    "Arylide Yellow" to intArrayOf(91, 84, 42),
    "Ash Gray" to intArrayOf(70, 75, 71),
    "Atomic Tangerine" to intArrayOf(100, 60, 40),
    "Aureolin" to intArrayOf(99, 93, 0),
    "Azure" to intArrayOf(0, 50, 100),
    "Azure (X11/Web)" to intArrayOf(94, 100, 100),
    "Baby Blue" to intArrayOf(54, 81, 94),
    "Baby Blue Eyes" to intArrayOf(63, 79, 95),
    "Baby Pink" to intArrayOf(96, 76, 76),
    "Baby Powder" to intArrayOf(100, 100, 98),
    "Baker-Miller Pink" to intArrayOf(100, 57, 69),
    "Banana Mania" to intArrayOf(98, 91, 71),
    "Barbie Pink" to intArrayOf(85, 9, 52),
    "Barn Red" to intArrayOf(49, 4, 1),
    "Battleship Grey" to intArrayOf(52, 52, 51),
    "Beau Blue" to intArrayOf(74, 83, 90),
    "Beaver" to intArrayOf(62, 51, 44),
    "Beige" to intArrayOf(96, 96, 86),
    "B'dazzled Blue" to intArrayOf(18, 35, 58),
    "Big Dip O’ruby" to intArrayOf(61, 15, 26),
    "Bisque" to intArrayOf(100, 89, 77),
    "Bistre" to intArrayOf(24, 17, 12),
    "Bistre Brown" to intArrayOf(59, 44, 9),
    "Bitter Lemon" to intArrayOf(79, 88, 5),
    "Black" to intArrayOf(0, 0, 0),
    "Black Bean" to intArrayOf(24, 5, 1),
    "Black Coral" to intArrayOf(33, 38, 44),
    "Black Olive" to intArrayOf(23, 24, 21),
    "Black Shadows" to intArrayOf(75, 69, 70),
    "Blanched Almond" to intArrayOf(100, 92, 80),
    "Blast-off Bronze" to intArrayOf(65, 44, 39),
    "Bleu de France" to intArrayOf(19, 55, 91),
    "Blizzard Blue" to intArrayOf(67, 90, 93),
    "Blood Red" to intArrayOf(40, 0, 0),
    "Blue" to intArrayOf(0, 0, 100),
    "Blue (Crayola)" to intArrayOf(12, 46, 100),
    "Blue (Munsell)" to intArrayOf(0, 58, 69),
    "Blue (NCS)" to intArrayOf(0, 53, 74),
    "Blue (Pantone)" to intArrayOf(0, 9, 66),
    "Blue (Pigment)" to intArrayOf(20, 20, 60),
    "Blue Bell" to intArrayOf(64, 64, 82),
    "Blue-Gray (Crayola)" to intArrayOf(40, 60, 80),
    "Blue Jeans" to intArrayOf(36, 68, 93),
    "Blue Sapphire" to intArrayOf(7, 38, 50),
    "Blue-Violet" to intArrayOf(54, 17, 89),
    "Blue Yonder" to intArrayOf(31, 45, 65),
    "Bluetiful" to intArrayOf(24, 41, 91),
    "Blush" to intArrayOf(87, 36, 51),
    "Bole" to intArrayOf(47, 27, 23),
    "Bone" to intArrayOf(89, 85, 79),
    "Brick Red" to intArrayOf(80, 25, 33),
    "Bright Lilac" to intArrayOf(85, 57, 94),
    "Bright Yellow (Crayola)" to intArrayOf(100, 67, 11),
    "British Racing Green" to intArrayOf(0, 26, 15),
    "Bronze" to intArrayOf(80, 50, 20),
    "Brown" to intArrayOf(59, 29, 0),
    "Brown Sugar" to intArrayOf(69, 43, 30),
    "Bud Green" to intArrayOf(48, 71, 38),
    "Buff" to intArrayOf(100, 78, 50),
    "Burgundy" to intArrayOf(50, 0, 13),
    "Burlywood" to intArrayOf(87, 72, 53),
    "Burnished Brown" to intArrayOf(63, 48, 45),
    "Burnt Orange" to intArrayOf(80, 33, 0),
    "Burnt Sienna" to intArrayOf(91, 45, 32),
    "Burnt Umber" to intArrayOf(54, 20, 14),
    "Byzantine" to intArrayOf(74, 20, 64),
    "Byzantium" to intArrayOf(44, 16, 39),
    "Cadet Blue" to intArrayOf(37, 62, 63),
    "Cadet Grey" to intArrayOf(57, 64, 69),
    "Cadmium Green" to intArrayOf(0, 42, 24),
    "Cadmium Orange" to intArrayOf(93, 53, 18),
    "Café au Lait" to intArrayOf(65, 48, 36),
    "Café Noir" to intArrayOf(29, 21, 13),
    "Cambridge Blue" to intArrayOf(64, 76, 68),
    "Camel" to intArrayOf(76, 60, 42),
    "Cameo Pink" to intArrayOf(94, 73, 80),
    "Canary" to intArrayOf(100, 100, 60),
    "Canary Yellow" to intArrayOf(100, 94, 0),
    "Candy Pink" to intArrayOf(89, 44, 48),
    "Cardinal" to intArrayOf(77, 12, 23),
    "Caribbean Green" to intArrayOf(0, 80, 60),
    "Carmine" to intArrayOf(59, 0, 9),
    "Carmine (M&P)" to intArrayOf(84, 0, 25),
    "Carnation Pink" to intArrayOf(100, 65, 79),
    "Carnelian" to intArrayOf(70, 11, 11),
    "Carolina Blue" to intArrayOf(34, 63, 83),
    "Carrot Orange" to intArrayOf(93, 57, 13),
    "Catawba" to intArrayOf(44, 21, 26),
    "Cedar Chest" to intArrayOf(79, 35, 29),
    "Celadon" to intArrayOf(67, 88, 69),
    "Celeste" to intArrayOf(70, 100, 100),
    "Cerise" to intArrayOf(87, 19, 39),
    "Cerulean" to intArrayOf(0, 48, 65),
    "Cerulean Blue" to intArrayOf(16, 32, 75),
    "Cerulean Frost" to intArrayOf(43, 61, 76),
    "Cerulean (Crayola)" to intArrayOf(11, 67, 84),
    "Cerulean (RGB)" to intArrayOf(0, 25, 100),
    "Champagne" to intArrayOf(97, 91, 81),
    "Champagne Pink" to intArrayOf(95, 87, 81),
    "Charcoal" to intArrayOf(21, 27, 31),
    "Charm Pink" to intArrayOf(90, 56, 67),
    "Chartreuse (Web)" to intArrayOf(50, 100, 0),
    "Cherry Blossom Pink" to intArrayOf(100, 72, 77),
    "Chestnut" to intArrayOf(58, 27, 21),
    "Chili Red" to intArrayOf(89, 24, 16),
    "China Pink" to intArrayOf(87, 44, 63),
    "Chinese Red" to intArrayOf(67, 22, 12),
    "Chinese Violet" to intArrayOf(52, 38, 53),
    "Chinese Yellow" to intArrayOf(100, 70, 0),
    "Chocolate (Traditional)" to intArrayOf(48, 25, 0),
    "Chocolate (Web)" to intArrayOf(82, 41, 12),
    "Cinereous" to intArrayOf(60, 51, 48),
    "Cinnabar" to intArrayOf(89, 26, 20),
    "Cinnamon Satin" to intArrayOf(80, 38, 49),
    "Citrine" to intArrayOf(89, 82, 4),
    "Citron" to intArrayOf(62, 66, 12),
    "Claret" to intArrayOf(50, 9, 20),
    "Coffee" to intArrayOf(44, 31, 22),
    "Columbia Blue" to intArrayOf(73, 85, 92),
    "Congo Pink" to intArrayOf(97, 51, 47),
    "Cool Grey" to intArrayOf(55, 57, 67),
    "Copper" to intArrayOf(72, 45, 20),
    "Copper (Crayola)" to intArrayOf(85, 54, 40),
    "Copper Penny" to intArrayOf(68, 44, 41),
    "Copper Red" to intArrayOf(80, 43, 32),
    "Copper Rose" to intArrayOf(60, 40, 40),
    "Coquelicot" to intArrayOf(100, 22, 0),
    "Coral" to intArrayOf(100, 50, 31),
    "Coral Pink" to intArrayOf(97, 51, 47),
    "Cordovan" to intArrayOf(54, 25, 27),
    "Corn" to intArrayOf(98, 93, 36),
    "Cornflower Blue" to intArrayOf(39, 58, 93),
    "Cornsilk" to intArrayOf(100, 97, 86),
    "Cosmic Cobalt" to intArrayOf(18, 18, 53),
    "Cosmic Latte" to intArrayOf(100, 97, 91),
    "Coyote Brown" to intArrayOf(51, 38, 24),
    "Cotton Candy" to intArrayOf(100, 74, 85),
    "Cream" to intArrayOf(100, 99, 82),
    "Crimson" to intArrayOf(86, 8, 24),
    "Crimson (UA)" to intArrayOf(62, 11, 20),
    "Cultured Pearl" to intArrayOf(96, 96, 96),
    "Cyan" to intArrayOf(0, 100, 100),
    "Cyan (Process)" to intArrayOf(0, 72, 92),
    "Cyber Grape" to intArrayOf(35, 26, 49),
    "Cyber Yellow" to intArrayOf(100, 83, 0),
    "Cyclamen" to intArrayOf(96, 44, 63),
    "Dandelion" to intArrayOf(100, 85, 36),
    "Dark Brown" to intArrayOf(40, 26, 13),
    "Dark Byzantium" to intArrayOf(36, 22, 33),
    "Dark Cyan" to intArrayOf(0, 55, 55),
    "Dark Electric Blue" to intArrayOf(33, 41, 47),
    "Dark Goldenrod" to intArrayOf(72, 53, 4),
    "Dark Green (X11)" to intArrayOf(0, 39, 0),
    "Dark Jungle Green" to intArrayOf(10, 14, 13),
    "Dark Khaki" to intArrayOf(74, 72, 42),
    "Dark Lava" to intArrayOf(28, 24, 20),
    "Dark Liver (Horses)" to intArrayOf(33, 24, 22),
    "Dark Magenta" to intArrayOf(55, 0, 55),
    "Dark Olive Green" to intArrayOf(33, 42, 18),
    "Dark Orange" to intArrayOf(100, 55, 0),
    "Dark Orchid" to intArrayOf(60, 20, 80),
    "Dark Purple" to intArrayOf(19, 10, 20),
    "Dark Red" to intArrayOf(55, 0, 0),
    "Dark Salmon" to intArrayOf(91, 59, 48),
    "Dark Sea Green" to intArrayOf(56, 74, 56),
    "Dark Sienna" to intArrayOf(24, 8, 8),
    "Dark Sky Blue" to intArrayOf(55, 75, 84),
    "Dark Slate Blue" to intArrayOf(28, 24, 55),
    "Dark Slate Gray" to intArrayOf(18, 31, 31),
    "Dark Spring Green" to intArrayOf(9, 45, 27),
    "Dark Turquoise" to intArrayOf(0, 81, 82),
    "Dark Violet" to intArrayOf(58, 0, 83),
    "Davy's Grey" to intArrayOf(33, 33, 33),
    "Deep Cerise" to intArrayOf(85, 20, 53),
    "Deep Champagne" to intArrayOf(98, 84, 65),
    "Deep Chestnut" to intArrayOf(73, 31, 28),
    "Deep Jungle Green" to intArrayOf(0, 29, 29),
    "Deep Pink" to intArrayOf(100, 8, 58),
    "Deep Saffron" to intArrayOf(100, 60, 20),
    "Deep Sky Blue" to intArrayOf(0, 75, 100),
    "Deep Space Sparkle" to intArrayOf(29, 39, 42),
    "Deep Taupe" to intArrayOf(49, 37, 38),
    "Denim" to intArrayOf(8, 38, 74),
    "Denim Blue" to intArrayOf(13, 26, 71),
    "Desert" to intArrayOf(76, 60, 42),
    "Desert Sand" to intArrayOf(93, 79, 69),
    "Dim Gray" to intArrayOf(41, 41, 41),
    "Dodger Blue" to intArrayOf(12, 56, 100),
    "Drab Dark Brown" to intArrayOf(29, 25, 16),
    "Duke Blue" to intArrayOf(0, 0, 61),
    "Dutch White" to intArrayOf(94, 87, 73),
    "Ebony" to intArrayOf(33, 36, 31),
    "Ecru" to intArrayOf(76, 70, 50),
    "Eerie Black" to intArrayOf(11, 11, 11),
    "Eggplant" to intArrayOf(38, 25, 32),
    "Eggshell" to intArrayOf(94, 92, 84),
    "Electric Lime" to intArrayOf(80, 100, 0),
    "Electric Purple" to intArrayOf(75, 0, 100),
    "Electric Violet" to intArrayOf(56, 0, 100),
    "Emerald" to intArrayOf(31, 78, 47),
    "Eminence" to intArrayOf(42, 19, 51),
    "English Lavender" to intArrayOf(71, 51, 58),
    "English Red" to intArrayOf(67, 29, 32),
    "English Vermillion" to intArrayOf(80, 28, 29),
    "English Violet" to intArrayOf(34, 24, 36),
    "Erin" to intArrayOf(0, 100, 25),
    "Eton Blue" to intArrayOf(59, 78, 64),
    "Fallow" to intArrayOf(76, 60, 42),
    "Falu Red" to intArrayOf(50, 9, 9),
    "Fandango" to intArrayOf(71, 20, 54),
    "Fandango Pink" to intArrayOf(87, 32, 52),
    "Fawn" to intArrayOf(90, 67, 44),
    "Fern Green" to intArrayOf(31, 47, 26),
    "Field Drab" to intArrayOf(42, 33, 12),
    "Fiery Rose" to intArrayOf(100, 33, 44),
    "Finn" to intArrayOf(41, 19, 41),
    "Firebrick" to intArrayOf(70, 13, 13),
    "Fire Engine Red" to intArrayOf(81, 13, 16),
    "Flame" to intArrayOf(89, 35, 13),
    "Flax" to intArrayOf(93, 86, 51),
    "Flirt" to intArrayOf(64, 0, 43),
    "Floral White" to intArrayOf(100, 98, 94),
    "Forest Green (Web)" to intArrayOf(13, 55, 13),
    "French Beige" to intArrayOf(65, 48, 36),
    "French Bistre" to intArrayOf(52, 43, 30),
    "French Blue" to intArrayOf(0, 45, 73),
    "French Fuchsia" to intArrayOf(99, 25, 57),
    "French Lilac" to intArrayOf(53, 38, 56),
    "French Lime" to intArrayOf(62, 99, 22),
    "French Mauve" to intArrayOf(83, 45, 83),
    "French Pink" to intArrayOf(99, 42, 62),
    "French Raspberry" to intArrayOf(78, 17, 28),
    "French Sky Blue" to intArrayOf(47, 71, 100),
    "French Violet" to intArrayOf(53, 2, 81),
    "Frostbite" to intArrayOf(91, 21, 65),
    "Fuchsia" to intArrayOf(100, 0, 100),
    "Fuchsia (Crayola)" to intArrayOf(76, 33, 76),
    "Fulvous" to intArrayOf(89, 52, 0),
    "Fuzzy Wuzzy" to intArrayOf(53, 26, 12),
    "Gainsboro" to intArrayOf(220, 220, 220),
    "Gamboge" to intArrayOf(228, 155, 15),
    "Generic viridian" to intArrayOf(0, 127, 102),
    "Ghost white" to intArrayOf(248, 248, 255),
    "Glaucous" to intArrayOf(96, 130, 182),
    "Glossy grape" to intArrayOf(171, 146, 179),
    "GO green" to intArrayOf(0, 171, 102),
    "Gold (metallic)" to intArrayOf(212, 175, 55),
    "Gold (web)" to intArrayOf(255, 215, 0),
    "Grullo" to intArrayOf(169, 154, 134),
    "Guardsman red" to intArrayOf(186, 0, 17),
    "Gunmetal" to intArrayOf(42, 52, 57),
    "Halaya úbe" to intArrayOf(102, 56, 84),
    "Han blue" to intArrayOf(68, 108, 179),
    "Han purple" to intArrayOf(82, 24, 250),
    "Harlequin" to intArrayOf(63, 255, 0),
    "Harvard crimson" to intArrayOf(182, 24, 29),
    "Harvest gold" to intArrayOf(218, 165, 32),
    "Heartland" to intArrayOf(163, 19, 17),
    "Heliotrope" to intArrayOf(223, 115, 255),
    "Hollywood cerulean" to intArrayOf(31, 117, 254),
    "Honeydew" to intArrayOf(240, 255, 240),
    "Hooker’s green" to intArrayOf(73, 121, 107),
    "Hot pink" to intArrayOf(255, 105, 180),
    "Hunter green" to intArrayOf(53, 94, 59),
    "Ice blue" to intArrayOf(201, 255, 255),
    "Icterine" to intArrayOf(252, 247, 94),
    "Imperial red" to intArrayOf(249, 73, 60),
    "Inchworm" to intArrayOf(178, 236, 93),
    "Independence" to intArrayOf(76, 81, 109),
    "India green" to intArrayOf(19, 136, 8),
    "Indian red" to intArrayOf(205, 92, 92),
    "Indian yellow" to intArrayOf(227, 168, 87),
    "Indigo dye" to intArrayOf(24, 26, 102),
    "Indigo (web)" to intArrayOf(75, 0, 130),
    "Ivory" to intArrayOf(255, 255, 240),
    "Jade" to intArrayOf(0, 168, 107),
    "Japanese indigo" to intArrayOf(38, 49, 60),
    "Jasmine" to intArrayOf(252, 255, 168),
    "Jellybean" to intArrayOf(229, 83, 138),
    "Jonquil" to intArrayOf(255, 255, 64),
    "June bud" to intArrayOf(189, 204, 127),
    "Jungle green" to intArrayOf(41, 171, 135),
    "Kelly green" to intArrayOf(76, 187, 23),
    "Khaki (web)" to intArrayOf(195, 176, 145),
    "Languid lavender" to intArrayOf(214, 202, 221),
    "Lapis lazuli" to intArrayOf(38, 97, 156),
    "Laser lemon" to intArrayOf(254, 254, 34),
    "Laurel green" to intArrayOf(169, 186, 156),
    "Lavender" to intArrayOf(230, 230, 250),
    "Lavender blue" to intArrayOf(204, 204, 255),
    "Lavender blush" to intArrayOf(255, 240, 245),
    "Lavender gray" to intArrayOf(196, 195, 208),
    "Lawn green" to intArrayOf(124, 252, 0),
    "Lemon" to intArrayOf(255, 247, 0),
    "Lemon chiffon" to intArrayOf(255, 250, 205),
    "Light blue" to intArrayOf(173, 216, 230),
    "Light brown" to intArrayOf(181, 101, 29),
    "Light coral" to intArrayOf(240, 128, 128),
    "Light cyan" to intArrayOf(224, 255, 255),
    "Light goldenrod yellow" to intArrayOf(250, 250, 210),
    "Light gray" to intArrayOf(211, 211, 211),
    "Light green" to intArrayOf(144, 238, 144),
    "Light pink" to intArrayOf(255, 182, 193),
    "Light salmon" to intArrayOf(255, 160, 122),
    "Light sea green" to intArrayOf(32, 178, 170),
    "Light sky blue" to intArrayOf(135, 206, 250),
    "Light slate gray" to intArrayOf(119, 136, 153),
    "Light steel blue" to intArrayOf(176, 196, 222),
    "Light yellow" to intArrayOf(255, 255, 224),
    "Lime" to intArrayOf(50, 205, 50),
    "Lime green" to intArrayOf(50, 205, 50),
    "Linen" to intArrayOf(250, 240, 230),
    "Livid" to intArrayOf(77, 77, 77),
    "Macaroni and cheese" to intArrayOf(255, 189, 136),
    "Magic mint" to intArrayOf(170, 240, 209),
    "Magenta" to intArrayOf(255, 0, 255),
    "Mahogany" to intArrayOf(192, 64, 0),
    "Manatee" to intArrayOf(151, 154, 170),
    "Mango tango" to intArrayOf(255, 130, 67),
    "Mantis" to intArrayOf(116, 185, 101),
    "Mardi gras" to intArrayOf(136, 0, 133),
    "Maroon" to intArrayOf(128, 0, 0),
    "Mauve" to intArrayOf(224, 176, 255),
    "Mauve taupe" to intArrayOf(145, 95, 109),
    "Melon" to intArrayOf(227, 168, 105),
    "Midnight blue" to intArrayOf(25, 25, 112),
    "Mint green" to intArrayOf(152, 255, 152),
    "Misty rose" to intArrayOf(255, 228, 225),
    "Moss green" to intArrayOf(138, 154, 91),
    "Misty rose" to intArrayOf(255, 228, 225),
    "Moccasin" to intArrayOf(255, 228, 181),
    "Mode beige" to intArrayOf(150, 113, 23),
    "Mona Lisa" to intArrayOf(255, 148, 142),
    "Morning blue" to intArrayOf(141, 163, 153),
    "Moss green" to intArrayOf(138, 154, 91),
    "Mountain Meadow" to intArrayOf(48, 186, 143),
    "Mountbatten pink" to intArrayOf(153, 122, 141),
    "MSU green" to intArrayOf(24, 69, 59),
    "Mulberry" to intArrayOf(197, 75, 140),
    "Mulberry (Crayola)" to intArrayOf(200, 80, 155),
    "Mustard" to intArrayOf(255, 219, 88),
    "Myrtle green" to intArrayOf(49, 120, 115),
    "Mystic" to intArrayOf(214, 82, 130),
    "Mystic maroon" to intArrayOf(173, 67, 121),
    "Nadeshiko pink" to intArrayOf(246, 173, 198),
    "Naples yellow" to intArrayOf(250, 218, 94),
    "Navajo white" to intArrayOf(255, 222, 173),
    "Navy blue" to intArrayOf(0, 0, 128),
    "Navy blue (Crayola)" to intArrayOf(25, 116, 210),
    "Neon blue" to intArrayOf(70, 102, 255),
    "Neon green" to intArrayOf(57, 255, 20),
    "Neon fuchsia" to intArrayOf(254, 65, 100),
    "New Car" to intArrayOf(33, 79, 198),
    "New York pink" to intArrayOf(215, 131, 127),
    "Nickel" to intArrayOf(114, 116, 114),
    "Non-photo blue" to intArrayOf(164, 221, 237),
    "Nyanza" to intArrayOf(233, 255, 219),
    "Ochre" to intArrayOf(204, 119, 34),
    "Old burgundy" to intArrayOf(67, 48, 46),
    "Old gold" to intArrayOf(207, 181, 59),
    "Old lace" to intArrayOf(253, 245, 230),
    "Old lavender" to intArrayOf(121, 104, 120),
    "Old mauve" to intArrayOf(103, 49, 71),
    "Old rose" to intArrayOf(192, 128, 129),
    "Old silver" to intArrayOf(132, 132, 130),
    "Olive" to intArrayOf(128, 128, 0),
    "Olive Drab (#3)" to intArrayOf(107, 142, 35),
    "Olive Drab #7" to intArrayOf(60, 52, 31),
    "Olive green" to intArrayOf(181, 179, 92),
    "Olivine" to intArrayOf(154, 185, 115),
    "Onyx" to intArrayOf(53, 56, 57),
    "Opal" to intArrayOf(168, 195, 188),
    "Opera mauve" to intArrayOf(183, 132, 167),
    "Orange" to intArrayOf(255, 127, 0),
    "Orange (Crayola)" to intArrayOf(255, 117, 56),
    "Orange (Pantone)" to intArrayOf(255, 88, 0),
    "Orange (web)" to intArrayOf(255, 165, 0),
    "Orange peel" to intArrayOf(255, 159, 0),
    "Orange-red" to intArrayOf(255, 104, 31),
    "Orange-red (Crayola)" to intArrayOf(255, 83, 73),
    "Orange soda" to intArrayOf(250, 91, 61),
    "Orange-yellow" to intArrayOf(245, 189, 31),
    "Orange-yellow (Crayola)" to intArrayOf(248, 213, 104),
    "Orchid" to intArrayOf(218, 112, 214),
    "Orchid pink" to intArrayOf(242, 189, 205),
    "Orchid (Crayola)" to intArrayOf(226, 156, 210),
    "Outer space (Crayola)" to intArrayOf(45, 56, 58),
    "Outrageous Orange" to intArrayOf(255, 110, 74),
    "Oxblood" to intArrayOf(74, 0, 0),
    "Oxford blue" to intArrayOf(0, 33, 71),
    "OU Crimson red" to intArrayOf(132, 22, 23),
    "Pacific blue" to intArrayOf(28, 169, 201),
    "Pakistan green" to intArrayOf(0, 102, 0),
    "Palatinate purple" to intArrayOf(104, 40, 96),
    "Pale aqua" to intArrayOf(190, 211, 229),
    "Pale cerulean" to intArrayOf(155, 196, 226),
    "Pale Dogwood" to intArrayOf(237, 122, 155),
    "Pale pink" to intArrayOf(250, 218, 221),
    "Pale purple (Pantone)" to intArrayOf(250, 230, 250),
    "Pale spring bud" to intArrayOf(236, 235, 189),
    "Pansy purple" to intArrayOf(120, 24, 74),
    "Paolo Veronese green" to intArrayOf(0, 155, 125),
    "Papaya whip" to intArrayOf(255, 239, 213),
    "Paradise pink" to intArrayOf(230, 62, 98),
    "Parchment" to intArrayOf(241, 233, 210),
    "Paris Green" to intArrayOf(80, 200, 120),
    "Pastel pink" to intArrayOf(222, 165, 164),
    "Patriarch" to intArrayOf(128, 0, 128),
    "Paua" to intArrayOf(31, 0, 94),
    "Payne's grey" to intArrayOf(83, 104, 120),
    "Peach" to intArrayOf(255, 229, 180),
    "Peach (Crayola)" to intArrayOf(255, 203, 164),
    "Peach puff" to intArrayOf(255, 218, 185),
    "Pear" to intArrayOf(209, 226, 49),
    "Pearly purple" to intArrayOf(183, 104, 162),
    "Periwinkle" to intArrayOf(204, 204, 255),
    "Periwinkle (Crayola)" to intArrayOf(195, 205, 230),
    "Permanent Geranium Lake" to intArrayOf(225, 44, 44),
    "Persian blue" to intArrayOf(28, 57, 187),
    "Persian green" to intArrayOf(0, 166, 147),
    "Persian indigo" to intArrayOf(50, 18, 122),
    "Persian orange" to intArrayOf(217, 144, 88),
    "Persian pink" to intArrayOf(247, 127, 190),
    "Persian plum" to intArrayOf(112, 28, 28),
    "Persian red" to intArrayOf(204, 51, 51),
    "Persian rose" to intArrayOf(254, 40, 162),
    "Persimmon" to intArrayOf(236, 88, 0),
    "Pewter Blue" to intArrayOf(139, 168, 183),
    "Phlox" to intArrayOf(223, 0, 255),
    "Phthalo blue" to intArrayOf(0, 15, 137),
    "Phthalo green" to intArrayOf(18, 53, 36),
    "Picotee blue" to intArrayOf(46, 39, 135),
    "Pictorial carmine" to intArrayOf(195, 11, 78),
    "Piggy pink" to intArrayOf(253, 221, 230),
    "Pine green" to intArrayOf(1, 121, 111),
    "Pine green" to intArrayOf(42, 47, 35),
    "Pink" to intArrayOf(255, 192, 203),
    "Pink (Pantone)" to intArrayOf(215, 72, 148),
    "Pink lace" to intArrayOf(255, 221, 244),
    "Pink lavender" to intArrayOf(216, 178, 209),
    "Pink Sherbet" to intArrayOf(247, 143, 167),
    "Pistachio" to intArrayOf(147, 197, 114),
    "Platinum" to intArrayOf(229, 228, 226),
    "Plum" to intArrayOf(142, 69, 133),
    "Plum (web)" to intArrayOf(221, 160, 221),
    "Plump Purple" to intArrayOf(89, 70, 178),
    "Polished Pine" to intArrayOf(93, 164, 147),
    "Pomp and Power" to intArrayOf(134, 96, 142),
    "Poppy red" to intArrayOf(255, 80, 80),
    "Portland Orange" to intArrayOf(255, 79, 0),
    "Powder blue" to intArrayOf(176, 224, 230),
    "Princeton orange" to intArrayOf(255, 143, 0),
    "Process Blue" to intArrayOf(0, 150, 214),
    "Prune" to intArrayOf(112, 28, 28),
    "Psychedelic purple" to intArrayOf(204, 50, 153),
    "Purple" to intArrayOf(128, 0, 128),
    "Purple (Crayola)" to intArrayOf(160, 32, 240),
    "Purple (web)" to intArrayOf(128, 0, 128),
    "Purple Heart" to intArrayOf(105, 53, 156),
    "Purple Mountain's Majesty" to intArrayOf(150, 120, 182),
    "Purple navy" to intArrayOf(50, 0, 66),
    "Purple Red" to intArrayOf(150, 39, 143),
    "Raspberry" to intArrayOf(227, 11, 92),
    "Raspberry Pink" to intArrayOf(226, 80, 152),
    "Raspberry Red" to intArrayOf(227, 11, 92),
    "Raw sienna" to intArrayOf(199, 97, 20),
    "Razzle dazzle rose" to intArrayOf(255, 51, 204),
    "Razzmatazz" to intArrayOf(227, 11, 92),
    "Red" to intArrayOf(255, 0, 0),
    "Red (Crayola)" to intArrayOf(255, 35, 39),
    "Red (Pantone)" to intArrayOf(186, 0, 47),
    "Red (Web)" to intArrayOf(255, 0, 0),
    "Red ochre" to intArrayOf(204, 104, 0),
    "Red-orange" to intArrayOf(255, 83, 73),
    "Red salsa" to intArrayOf(255, 64, 64),
    "Red-violet" to intArrayOf(199, 21, 133),
    "Robins egg blue" to intArrayOf(0, 204, 204),
    "Rose" to intArrayOf(255, 0, 127),
    "Rose Bonbon" to intArrayOf(234, 83, 107),
    "Rose Dust" to intArrayOf(159, 126, 133),
    "Rose gold" to intArrayOf(183, 110, 121),
    "Rose Madder" to intArrayOf(227, 38, 54),
    "Rose pink" to intArrayOf(255, 102, 204),
    "Rose Red" to intArrayOf(194, 30, 86),
    "Rosewood" to intArrayOf(101, 0, 11),
    "Royal blue" to intArrayOf(65, 105, 225),
    "Royal purple" to intArrayOf(120, 81, 169),
    "Royal red" to intArrayOf(175, 0, 42),
    "Rubine red" to intArrayOf(209, 0, 86),
    "Ruby" to intArrayOf(224, 17, 95),
    "Rufous" to intArrayOf(168, 28, 43),
    "Salmon" to intArrayOf(250, 128, 114),
    "Salmon pink" to intArrayOf(255, 145, 164),
    "Sand" to intArrayOf(194, 178, 128),
    "Sand Dune" to intArrayOf(150, 113, 23),
    "Sandy brown" to intArrayOf(244, 164, 96),
    "Sap green" to intArrayOf(80, 125, 53),
    "Sapphire" to intArrayOf(15, 82, 186),
    "Satin sheen gold" to intArrayOf(203, 161, 53),
    "Scarlet" to intArrayOf(255, 36, 0),
    "Scarlet (Crayola)" to intArrayOf(255, 0, 57),
    "Schauss pink" to intArrayOf(255, 145, 191),
    "School bus yellow" to intArrayOf(255, 216, 0),
    "Sea green" to intArrayOf(46, 139, 87),
    "Sea green (Crayola)" to intArrayOf(0, 204, 204),
    "Seal brown" to intArrayOf(50, 20, 20),
    "Seashell" to intArrayOf(255, 245, 238),
    "Selective yellow" to intArrayOf(255, 186, 0),
    "Sepia" to intArrayOf(112, 66, 20),
    "Shadow blue" to intArrayOf(119, 136, 153),
    "Shamrock green" to intArrayOf(0, 158, 96),
    "Shampoo" to intArrayOf(255, 207, 230),
    "Shocking pink" to intArrayOf(252, 15, 192),
    "Shocking pink (Crayola)" to intArrayOf(252, 15, 192),
    "Sienna" to intArrayOf(136, 45, 23),
    "Silver" to intArrayOf(192, 192, 192),
    "Silver (Crayola)" to intArrayOf(192, 192, 192),
    "Silver Pink" to intArrayOf(196, 174, 173),
    "Sinopia" to intArrayOf(203, 65, 11),
    "Sky blue" to intArrayOf(135, 206, 235),
    "Sky blue (Crayola)" to intArrayOf(135, 206, 250),
    "Slate blue" to intArrayOf(106, 90, 205),
    "Slate gray" to intArrayOf(112, 128, 144),
    "Smoky black" to intArrayOf(16, 12, 8),
    "Snow" to intArrayOf(255, 250, 250),
    "Space cadet" to intArrayOf(48, 25, 52),
    "Spanish brown" to intArrayOf(150, 73, 51),
    "Spanish gray" to intArrayOf(153, 153, 153),
    "Spanish red" to intArrayOf(230, 0, 0),
    "Spanish violet" to intArrayOf(76, 40, 130),
    "Spring green" to intArrayOf(0, 255, 127),
    "Spring green (Crayola)" to intArrayOf(0, 255, 127),
    "Star command blue" to intArrayOf(0, 35, 65),
    "Steel blue" to intArrayOf(70, 130, 180),
    "Stil de grain yellow" to intArrayOf(250, 235, 215),
    "Straw" to intArrayOf(228, 217, 111),
    "Sugar pink" to intArrayOf(255, 206, 220),
    "Sunburst yellow" to intArrayOf(255, 184, 28),
    "Sunglow" to intArrayOf(255, 204, 51),
    "Sunset orange" to intArrayOf(253, 94, 83),
    "Tan" to intArrayOf(210, 180, 140),
    "Tan (Crayola)" to intArrayOf(196, 164, 132),
    "Tangerine" to intArrayOf(242, 133, 0),
    "Tangerine yellow" to intArrayOf(252, 198, 0),
    "Tea green" to intArrayOf(208, 240, 192),
    "Tea rose" to intArrayOf(246, 205, 186),
    "Teal" to intArrayOf(0, 128, 128),
    "Teal Blue" to intArrayOf(54, 117, 136),
    "Turquoise" to intArrayOf(64, 224, 208),
    "Turquoise blue" to intArrayOf(0, 255, 255),
    "Turquoise Green" to intArrayOf(48, 213, 200),
    "Tuscan brown" to intArrayOf(111, 78, 55),
    "Tuscan red" to intArrayOf(124, 72, 56),
    "Twilight lavender" to intArrayOf(138, 73, 107),
    "Tyrian purple" to intArrayOf(102, 2, 60),
    "Ultra pink" to intArrayOf(255, 111, 255),
    "Ultra red" to intArrayOf(84, 0, 0),
    "Ultramarine" to intArrayOf(18, 10, 143),
    "Ultramarine blue" to intArrayOf(0, 102, 255),
    "United Nations blue" to intArrayOf(0, 102, 204),
    "Unmellow yellow" to intArrayOf(255, 255, 102),
    "Up Forest Green" to intArrayOf(1, 68, 33),
    "Urobilin" to intArrayOf(255, 194, 0),
    "Vanilla" to intArrayOf(243, 229, 171),
    "Vanilla ice" to intArrayOf(243, 229, 171),
    "Violet" to intArrayOf(238, 130, 238),
    "Violet (Crayola)" to intArrayOf(159, 43, 104),
    "Violet (web)" to intArrayOf(238, 130, 238),
    "Violet-blue" to intArrayOf(50, 74, 178),
    "Violet-red" to intArrayOf(247, 83, 139),
    "Viridian" to intArrayOf(64, 130, 109),
    "Viridian Green" to intArrayOf(48, 98, 49),
    "Vivid amber" to intArrayOf(255, 191, 0),
    "Vivid blue" to intArrayOf(0, 102, 255),
    "Vivid cerulean" to intArrayOf(0, 150, 255),
    "Vivid crimson" to intArrayOf(255, 0, 35),
    "Vivid pink" to intArrayOf(255, 0, 255),
    "Vivid red" to intArrayOf(255, 0, 0),
    "Vivid violet" to intArrayOf(159, 0, 255),
    "Wheat" to intArrayOf(245, 222, 179),
    "White" to intArrayOf(255, 255, 255),
    "White smoke" to intArrayOf(245, 245, 245),
    "Wild blue yonder" to intArrayOf(162, 173, 208),
    "Wild strawberry" to intArrayOf(255, 89, 136),
    "Wild watermelon" to intArrayOf(252, 108, 133),
    "Wine" to intArrayOf(114, 47, 55),
    "Wine red" to intArrayOf(114, 47, 55),
    "Wisteria" to intArrayOf(201, 160, 220),
    "Yellow" to intArrayOf(255, 255, 0),
    "Yellow (Crayola)" to intArrayOf(254, 254, 34),
    "Yellow (Pantone)" to intArrayOf(255, 255, 0),
    "Yellow-green" to intArrayOf(154, 205, 50),
    "Yellow orange" to intArrayOf(255, 174, 66),
    "Zaffre" to intArrayOf(0, 20, 168),
    "Zinnwaldite brown" to intArrayOf(44, 22, 8),
    "Zomp" to intArrayOf(57, 255, 20)
)

val predefinedColors = mapOf(
    "Absolute Zero" to Color(0xFF0048BA).toIntArray(),
    "Acid Green" to Color(0xFFB0BF1A).toIntArray(),
    "Aero" to Color(0xFF7CB9E8).toIntArray(),
    "African Violet" to Color(0xFFB284BE).toIntArray(),
    "Air Superiority Blue" to Color(0xFF72A0C1).toIntArray(),
    "Alabaster" to Color(0xFFF2F0E6).toIntArray(),
    "Alice Blue" to Color(0xFFF0F8FF).toIntArray(),
    "Alizarin" to Color(0xFFDB2D43).toIntArray(),
    "Alloy Orange" to Color(0xFFC46210).toIntArray(),
    "Almond" to Color(0xFFEED9C4).toIntArray(),
    "Amaranth" to Color(0xFFE52B50).toIntArray(),
    "Amaranth Deep Purple" to Color(0xFF9F2B68).toIntArray(),
    "Amaranth Pink" to Color(0xFFF19CBB).toIntArray(),
    "Amaranth Purple" to Color(0xFFAB274F).toIntArray(),
    "Amazon" to Color(0xFF3B7A57).toIntArray(),
    "Amber" to Color(0xFFFFBF00).toIntArray(),
    "Amber (SAE/ECE)" to Color(0xFFFF7E00).toIntArray(),
    "Amethyst" to Color(0xFF9966CC).toIntArray(),
    "Amethyst (Crayola)" to Color(0xFF64609A).toIntArray(),
    "Android Green" to Color(0xFF3DDC84).toIntArray(),
    "Antique Brass" to Color(0xFFC88A65).toIntArray(),
    "Antique Bronze" to Color(0xFF665D1E).toIntArray(),
    "Antique Fuchsia" to Color(0xFF915C83).toIntArray(),
    "Antique Ruby" to Color(0xFF841B2D).toIntArray(),
    "Antique White" to Color(0xFFFAEBD7).toIntArray(),
    "Apricot" to Color(0xFFFBCEB1).toIntArray(),
    "Aqua" to Color(0xFF00FFFF).toIntArray(),
    "Aquamarine" to Color(0xFF7FFFD4).toIntArray(),
    "Aquamarine (Crayola)" to Color(0xFF95E0EB).toIntArray(),
    "Arctic Lime" to Color(0xFFD0FF14).toIntArray(),
    "Artichoke Green" to Color(0xFF4B6F44).toIntArray(),
    "Arylide Yellow" to Color(0xFFE9D66B).toIntArray(),
    "Ash Gray" to Color(0xFFB2BEB5).toIntArray(),
    "Asparagus" to Color(0xFF7BA05B).toIntArray(),
    "Atomic Tangerine" to Color(0xFFFF9966).toIntArray(),
    "Aureolin" to Color(0xFFFDEE00).toIntArray(),
    "Aztec Gold" to Color(0xFFC39953).toIntArray(),
    "Azure" to Color(0xFF007FFF).toIntArray(),
    "Azure (X11/web color)" to Color(0xFFF0FFFF).toIntArray(),
    "Baby Blue" to Color(0xFF89CFF0).toIntArray(),
    "Baby Blue Eyes" to Color(0xFFA1CAF1).toIntArray(),
    "Baby Pink" to Color(0xFFF4C2C2).toIntArray(),
    "Baby Powder" to Color(0xFFFEFEFA).toIntArray(),
    "Baker-Miller Pink" to Color(0xFFFF91AF).toIntArray(),
    "Banana Mania" to Color(0xFFFAE7B5).toIntArray(),
    "Barbie Pink" to Color(0xFFDA1884).toIntArray(),
    "Barn Red" to Color(0xFF7C0A02).toIntArray(),
    "Battleship Grey" to Color(0xFF848482).toIntArray(),
    "Beau Blue" to Color(0xFFBCD4E6).toIntArray(),
    "Beaver" to Color(0xFF9F8170).toIntArray(),
    "Beige" to Color(0xFFF5F5DC).toIntArray(),
    "Berry Parfait" to Color(0xFFA43482).toIntArray(),
    "B'dazzled Blue" to Color(0xFF2E5894).toIntArray(),
    "Big Dip O’ruby" to Color(0xFF9C2542).toIntArray(),
    "Big Foot Feet" to Color(0xFFE88E5A).toIntArray(),
    "Bisque" to Color(0xFFFFE4C4).toIntArray(),
    "Bistre" to Color(0xFF3D2B1F).toIntArray(),
    "Bistre Brown" to Color(0xFF967117).toIntArray(),
    "Bitter Lemon" to Color(0xFFCAE00D).toIntArray(),
    "Bittersweet" to Color(0xFFFE6F5E).toIntArray(),
    "Bittersweet Shimmer" to Color(0xFFBF4F51).toIntArray(),
    "Black" to Color(0xFF000000).toIntArray(),
    "Black Bean" to Color(0xFF3D0C02).toIntArray(),
    "Black Coral" to Color(0xFF54626F).toIntArray(),
    "Black Olive" to Color(0xFF3B3C36).toIntArray(),
    "Black Shadows" to Color(0xFFBFAFB2).toIntArray(),
    "Blanched Almond" to Color(0xFFFFEBCD).toIntArray(),
    "Blast-off Bronze" to Color(0xFFA57164).toIntArray(),
    "Bleu De France" to Color(0xFF318CE7).toIntArray(),
    "Blizzard Blue" to Color(0xFFACE5EE).toIntArray(),
    "Blood Red" to Color(0xFF660000).toIntArray(),
    "Blue" to Color(0xFF0000FF).toIntArray(),
    "Blue (Crayola)" to Color(0xFF1F75FE).toIntArray(),
    "Blue (Munsell)" to Color(0xFF0093AF).toIntArray(),
    "Blue (NCS)" to Color(0xFF0087BD).toIntArray(),
    "Blue (Pantone)" to Color(0xFF0018A8).toIntArray(),
    "Blue (Pigment)" to Color(0xFF333399).toIntArray(),
    "Blue Bell" to Color(0xFFA2A2D0).toIntArray(),
    "Blue-Green" to Color(0xFF0D98BA).toIntArray(),
    "Blue Jeans" to Color(0xFF5DADEC).toIntArray(),
    "Blue-Ribbon" to Color(0xFF0B10A2).toIntArray(),
    "Blue Violet" to Color(0xFF8A2BE2).toIntArray(),
    "Blue Sapphire" to Color(0xFF126180).toIntArray(),
    "Blue-Violet" to Color(0xFF8A2BE2).toIntArray(),
    "Blue Yonder" to Color(0xFF5072A7).toIntArray(),
    "Blueberry" to Color(0xFF4F86F7).toIntArray(),
    "Bluetiful" to Color(0xFF3C69E7).toIntArray(),
    "Blush" to Color(0xFFDE5D83).toIntArray(),
    "Bole" to Color(0xFF79443B).toIntArray(),
    "Bone" to Color(0xFFE3DAC9).toIntArray(),
    "Booger Buster" to Color(0xFFDDE26A).toIntArray(),
    "Brick Red" to Color(0xFFCB4154).toIntArray(),
    "Bright Green" to Color(0xFF66FF00).toIntArray(),
    "Bright Maroon" to Color(0xFFC32148).toIntArray(),
    "Bright Navy Blue" to Color(0xFF1974D2).toIntArray(),
    "Bright Lilac" to Color(0xFFD891EF).toIntArray(),
    "Bright Pink" to Color(0xFFFF007F).toIntArray(),
    "Bright Turquoise" to Color(0xFF08E8DE).toIntArray(),
    "Bright Yellow (Crayola)" to Color(0xFFFFAA1D).toIntArray(),
    "Brilliant Rose" to Color(0xFFE667CE).toIntArray(),
    "Brink Pink" to Color(0xFFFB607F).toIntArray(),
    "British Racing Green" to Color(0xFF004225).toIntArray(),
    "Bronze" to Color(0xFFCD7F32).toIntArray(),
    "Brown" to Color(0xFF964B00).toIntArray(),
    "Brown (Crayola)" to Color(0xFFAF593E).toIntArray(),
    "Brown (Web)" to Color(0xFFA52A2A).toIntArray(),
    "Brown Sugar" to Color(0xFFAF6E4D).toIntArray(),
    "Bud Green" to Color(0xFF7BB661).toIntArray(),
    "Buff" to Color(0xFFFFC680).toIntArray(),
    "Burgundy" to Color(0xFF800020).toIntArray(),
    "Burlywood" to Color(0xFFDEB887).toIntArray(),
    "Burnished Brown" to Color(0xFFA17A74).toIntArray(),
    "Burnt Orange" to Color(0xFFCC5500).toIntArray(),
    "Burnt Sienna" to Color(0xFFE97451).toIntArray(),
    "Burnt Umber" to Color(0xFF8A3324).toIntArray(),
    "Byzantine" to Color(0xFFBD33A4).toIntArray(),
    "Byzantium" to Color(0xFF702963).toIntArray(),
    "Cadet Blue" to Color(0xFF5F9EA0).toIntArray(),
    "Cadet Grey" to Color(0xFF91A3B0).toIntArray(),
    "Cadmium Green" to Color(0xFF006B3C).toIntArray(),
    "Cadmium Orange" to Color(0xFFED872D).toIntArray(),
    "Cadmium Red" to Color(0xFFE30022).toIntArray(),
    "Cadmium Yellow" to Color(0xFFFFF600).toIntArray(),
    "Café au Lait" to Color(0xFFA67B5B).toIntArray(),
    "Café Noir" to Color(0xFF4B3621).toIntArray(),
    "Cambridge Blue" to Color(0xFFA3C1AD).toIntArray(),
    "Camel" to Color(0xFFC19A6B).toIntArray(),
    "Cameo Pink" to Color(0xFFEFBBCC).toIntArray(),
    "Canary" to Color(0xFFFFFF99).toIntArray(),
    "Canary Yellow" to Color(0xFFFFEF00).toIntArray(),
    "Candy Apple Red" to Color(0xFFFF0800).toIntArray(),
    "Candy Pink" to Color(0xFFE4717A).toIntArray(),
    "Cardinal" to Color(0xFFC41E3A).toIntArray(),
    "Caribbean Green" to Color(0xFF00CC99).toIntArray(),
    "Carmine" to Color(0xFF960018).toIntArray(),
    "Carmine (M&P)" to Color(0xFFD70040).toIntArray(),
    "Carnation Pink" to Color(0xFFFFA6C9).toIntArray(),
    "Carnelian" to Color(0xFFB31B1B).toIntArray(),
    "Carolina Blue" to Color(0xFF56A0D3).toIntArray(),
    "Carrot Orange" to Color(0xFFED9121).toIntArray(),
    "Catawba" to Color(0xFF703642).toIntArray(),
    "Cedar Chest" to Color(0xFFC95A49).toIntArray(),
    "Celadon" to Color(0xFFACE1AF).toIntArray(),
    "Celeste" to Color(0xFFB2FFFF).toIntArray(),
    "Cerise" to Color(0xFFDE3163).toIntArray(),
    "Cerulean" to Color(0xFF007BA7).toIntArray(),
    "Cerulean Blue" to Color(0xFF2A52BE).toIntArray(),
    "Cerulean Frost" to Color(0xFF6D9BC3).toIntArray(),
    "Cerulean (Crayola)" to Color(0xFF1DACD6).toIntArray(),
    "Cerulean (RGB)" to Color(0xFF0040FF).toIntArray(),
    "Champagne" to Color(0xFFF7E7CE).toIntArray(),
    "Champagne Pink" to Color(0xFFF1DDCF).toIntArray(),
    "Charcoal" to Color(0xFF36454F).toIntArray(),
    "Charleston Green" to Color(0xFF232B2B).toIntArray(),
    "Charm Pink" to Color(0xFFE68FAC).toIntArray(),
    "Chartreuse" to Color(0xFF7FFF00).toIntArray(),
    "Chartreuse (Web)" to Color(0xFF80FF00).toIntArray(),
    "Cherry Blossom Pink" to Color(0xFFFFB7C5).toIntArray(),
    "Chestnut" to Color(0xFF954535).toIntArray(),
    "Chili Red" to Color(0xFFE23D28).toIntArray(),
    "China Pink" to Color(0xFFDE6FA1).toIntArray(),
    "Chinese Red" to Color(0xFFAA381E).toIntArray(),
    "Chinese Violet" to Color(0xFF856088).toIntArray(),
    "Chinese Yellow" to Color(0xFFFFB200).toIntArray(),
    "Chocolate (Traditional)" to Color(0xFF7B3F00).toIntArray(),
    "Chocolate (Web)" to Color(0xFFD2691E).toIntArray(),
    "Cinereous" to Color(0xFF98817B).toIntArray(),
    "Cinnabar" to Color(0xFFE34234).toIntArray(),
    "Cinnamon Satin" to Color(0xFFCD607E).toIntArray(),
    "Citrine" to Color(0xFFE4D00A).toIntArray(),
    "Citron" to Color(0xFF9FA91F).toIntArray(),
    "Claret" to Color(0xFF7F1734).toIntArray(),
    "Cobalt Blue" to Color(0xFF0047AB).toIntArray(),
    "Cocoa Brown" to Color(0xFFD2691E).toIntArray(),
    "Coconut" to Color(0xFF965A3E).toIntArray(),
    "Coffee" to Color(0xFF6F4E37).toIntArray(),
    "Columbia Blue" to Color(0xFFB9D9EB).toIntArray(),
    "Congo Pink" to Color(0xFFF88379).toIntArray(),
    "Cool Grey" to Color(0xFF8C92AC).toIntArray(),
    "Copper" to Color(0xFFB87333).toIntArray(),
    "Copper (Crayola)" to Color(0xFFDA8A67).toIntArray(),
    "Copper Penny" to Color(0xFFAD6F69).toIntArray(),
    "Copper Red" to Color(0xFFCB6D51).toIntArray(),
    "Copper Rose" to Color(0xFF996666).toIntArray(),
    "Coquelicot" to Color(0xFFFF3800).toIntArray(),
    "Coral" to Color(0xFFFF7F50).toIntArray(),
    "Coral Pink" to Color(0xFFF88379).toIntArray(),
    "Cordovan" to Color(0xFF893F45).toIntArray(),
    "Corn" to Color(0xFFFBEC5D).toIntArray(),
    "Cornflower Blue" to Color(0xFF6495ED).toIntArray(),
    "Cornflower Blue (Crayola)" to Color(0xFF93CCEA).toIntArray(),
    "Cornsilk" to Color(0xFFFFF8DC).toIntArray(),
    "Cosmic Cobalt" to Color(0xFF2E2D88).toIntArray(),
    "Cosmic Latte" to Color(0xFFFFF8E7).toIntArray(),
    "Coyote Brown" to Color(0xFF81613C).toIntArray(),
    "Cotton Candy" to Color(0xFFFFBCD9).toIntArray(),
    "Cream" to Color(0xFFFFFDD0).toIntArray(),
    "Crimson" to Color(0xFFDC143C).toIntArray(),
    "Crimson (UA)" to Color(0xFF9E1B32).toIntArray(),
    "Cultured Pearl" to Color(0xFFF5F5F5).toIntArray(),
    "Cyan" to Color(0xFF00FFFF).toIntArray(),
    "Cyan (Process)" to Color(0xFF00B7EB).toIntArray(),
    "Cyber Grape" to Color(0xFF58427C).toIntArray(),
    "Cyber Yellow" to Color(0xFFFFD300).toIntArray(),
    "Cyclamen" to Color(0xFFF56FA1).toIntArray(),
    "Dandelion" to Color(0xFFDDF719).toIntArray(),
    "Dark Blue" to Color(0xFF00008B).toIntArray(),
    "Dark Blue-Gray" to Color(0xFF666699).toIntArray(),
    "Dark Brown" to Color(0xFF654321).toIntArray(),
    "Dark Byzantium" to Color(0xFF5D3954).toIntArray(),
    "Dark Cyan" to Color(0xFF008B8B).toIntArray(),
    "Dark Electric Blue" to Color(0xFF536878).toIntArray(),
    "Dark Fuchsia" to Color(0xFFA00955).toIntArray(),
    "Dark Goldenrod" to Color(0xFFB8860B).toIntArray(),
    "Dark Gray" to Color(0xFFA9A9A9).toIntArray(),
    "Dark Green" to Color(0xFF013220).toIntArray(),
    "Dark Green (X11)" to Color(0xFF006400).toIntArray(),
    "Dark Jungle Green" to Color(0xFF1A2421).toIntArray(),
    "Dark Khaki" to Color(0xFFBDB76B).toIntArray(),
    "Dark Lava" to Color(0xFF483C32).toIntArray(),
    "Dark Liver (Horses)" to Color(0xFF543D37).toIntArray(),
    "Dark Liver (Dogs)" to Color(0xFFB87333).toIntArray(),
    "Dark Liver" to Color(0xFF534B4F).toIntArray(),
    "Dark Magenta" to Color(0xFF8B008B).toIntArray(),
    "Dark Midnight Blue" to Color(0xFF003366).toIntArray(),
    "Dark Moss Green" to Color(0xFF4A5D23).toIntArray(),
    "Dark Olive Green" to Color(0xFF556B2F).toIntArray(),
    "Dark Orange" to Color(0xFFFF8C00).toIntArray(),
    "Dark Orchid" to Color(0xFF9932CC).toIntArray(),
    "Dark Pastel Green" to Color(0xFF03C03C).toIntArray(),
    "Dark Purple" to Color(0xFF301934).toIntArray(),
    "Dark Raspberry" to Color(0xFF872657).toIntArray(),
    "Dark Red" to Color(0xFF8B0000).toIntArray(),
    "Dark Salmon" to Color(0xFFE9967A).toIntArray(),
    "Dark Sea Green" to Color(0xFF8FBC8F).toIntArray(),
    "Dark Sienna" to Color(0xFF3C1414).toIntArray(),
    "Dark Sky Blue" to Color(0xFF8CBED6).toIntArray(),
    "Dark Slate Blue" to Color(0xFF483D8B).toIntArray(),
    "Dark Slate Gray" to Color(0xFF2F4F4F).toIntArray(),
    "Dark Spring Green" to Color(0xFF177245).toIntArray(),
    "Dark Tan" to Color(0xFF918151).toIntArray(),
    "Dark Turquoise" to Color(0xFF00CED1).toIntArray(),
    "Dark Vanilla" to Color(0xFFD1BEA8).toIntArray(),
    "Dark Violet" to Color(0xFF9400D3).toIntArray(),
    "Dartmouth Green" to Color(0xFF00703C).toIntArray(),
    "Davy's Grey" to Color(0xFF555555).toIntArray(),
    "Deep Cerise" to Color(0xFFDA3287).toIntArray(),
    "Deep Champagne" to Color(0xFFFAD6A5).toIntArray(),
    "Deep Chestnut" to Color(0xFFB94E48).toIntArray(),
    "Deep Fuchsia" to Color(0xFFC154C1).toIntArray(),
    "Deep Jungle Green" to Color(0xFF004B49).toIntArray(),
    "Deep Lemon" to Color(0xFFF5C71A).toIntArray(),
    "Deep Mauve" to Color(0xFFD473D4).toIntArray(),
    "Deep Pink" to Color(0xFFFF1493).toIntArray(),
    "Deep Sky Blue" to Color(0xFF00BFFF).toIntArray(),
    "Deep Saffron" to Color(0xFFFF9933).toIntArray(),
    "Deep Sky Blue" to Color(0xFF00BFFF).toIntArray(),
    "Deep Space Sparkle" to Color(0xFF4A646C).toIntArray(),
    "Deep Taupe" to Color(0xFF7E5E60).toIntArray(),
    "Denim" to Color(0xFF1560BD).toIntArray(),
    "Denim Blue" to Color(0xFF2243B6).toIntArray(),
    "Desert" to Color(0xFFC19A6B).toIntArray(),
    "Desert Sand" to Color(0xFFEDC9AF).toIntArray(),
    "Dim Gray" to Color(0xFF696969).toIntArray(),
    "Dingy Dungeon" to Color(0xFFC53151).toIntArray(),
    "Dirt" to Color(0xFF9B7653).toIntArray(),
    "Dodger Blue" to Color(0xFF1E90FF).toIntArray(),
    "Dogwood Rose" to Color(0xFFD71868).toIntArray(),
    "Drab Dark Brown" to Color(0xFF4A412A).toIntArray(),
    "Duke Blue" to Color(0xFF00009C).toIntArray(),
    "Dutch White" to Color(0xFFEFDFBB).toIntArray(),
    "Earth Yellow" to Color(0xFFE1A95F).toIntArray(),
    "Ebony" to Color(0xFF555D50).toIntArray(),
    "Ecru" to Color(0xFFC2B280).toIntArray(),
    "Eerie Black" to Color(0xFF1B1B1B).toIntArray(),
    "Eggplant" to Color(0xFF614051).toIntArray(),
    "Eggshell" to Color(0xFFF0EAD6).toIntArray(),
    "Egyptian Blue" to Color(0xFF1034A6).toIntArray(),
    "Electric Blue" to Color(0xFF7DF9FF).toIntArray(),
    "Electric Indigo" to Color(0xFF6F00FF).toIntArray(),
    "Electric Lime" to Color(0xFFCCFF00).toIntArray(),
    "Electric Purple" to Color(0xFFBF00FF).toIntArray(),
    "Electric Violet" to Color(0xFF8F00FF).toIntArray(),
    "Emerald" to Color(0xFF50C878).toIntArray(),
    "Eminence" to Color(0xFF6C3082).toIntArray(),
    "English Lavender" to Color(0xFFB48395).toIntArray(),
    "English Red" to Color(0xFFAB4B52).toIntArray(),
    "English Vermillion" to Color(0xFFCC474B).toIntArray(),
    "English Violet" to Color(0xFF563C5C).toIntArray(),
    "Erin" to Color(0xFF00FF40).toIntArray(),
    "Eton Blue" to Color(0xFF96C8A2).toIntArray(),
    "Eucalyptus" to Color(0xFF44D7A8).toIntArray(),
    "Fallow" to Color(0xFFC19A6B).toIntArray(),
    "Falu Red" to Color(0xFF801818).toIntArray(),
    "Fandango" to Color(0xFFB53389).toIntArray(),
    "Fandango Pink" to Color(0xFFDE5285).toIntArray(),
    "Fashion Fuchsia" to Color(0xFFF400A1).toIntArray(),
    "Fawn" to Color(0xFFE5AA70).toIntArray(),
    "Feldgrau" to Color(0xFF4D5D53).toIntArray(),
    "Fern Green" to Color(0xFF4F7942).toIntArray(),
    "Field Drab" to Color(0xFF6C541E).toIntArray(),
    "Fiery Rose" to Color(0xFFFF5470).toIntArray(),
    "Finn" to Color(0xFF683068).toIntArray(),
    "Firebrick" to Color(0xFFB22222).toIntArray(),
    "Fire Engine Red" to Color(0xFFCE2029).toIntArray(),
    "Fire Opal" to Color(0xFFE95C4B).toIntArray(),
    "Flame" to Color(0xFFE25822).toIntArray(),
    "Flax" to Color(0xFFEEDC82).toIntArray(),
    "Flirt" to Color(0xFFA2006D).toIntArray(),
    "Floral White" to Color(0xFFFFFAF0).toIntArray(),
    "Fluorescent Blue" to Color(0xFF15F4EE).toIntArray(),
    "Forest Green (Crayola)" to Color(0xFF5FA777).toIntArray(),
    "Forest Green (Crayola)" to Color(0xFF014421).toIntArray(),
    "Forest Green (Web)" to Color(0xFF228B22).toIntArray(),
    "French Beige" to Color(0xFFA67B5B).toIntArray(),
    "French Bistre" to Color(0xFF856D4D).toIntArray(),
    "French Blue" to Color(0xFF0072BB).toIntArray(),
    "French Fuchsia" to Color(0xFFFD3F92).toIntArray(),
    "French Lilac" to Color(0xFF86608E).toIntArray(),
    "French Lime" to Color(0xFF9EFD38).toIntArray(),
    "French Mauve" to Color(0xFFD473D4).toIntArray(),
    "French Pink" to Color(0xFFFD6C9E).toIntArray(),
    "French Raspberry" to Color(0xFFC72C48).toIntArray(),
    "French Rose" to Color(0xFFF64A8A).toIntArray(),
    "French Sky Blue" to Color(0xFF77B5FE).toIntArray(),
    "French Violet" to Color(0xFF8806CE).toIntArray(),
    "Frostbite" to Color(0xFFE936A7).toIntArray(),
    "Fuchsia" to Color(0xFFFF00FF).toIntArray(),
    "Fuchsia (Crayola)" to Color(0xFFC154C1).toIntArray(),
    "Fuchsia Purple" to Color(0xFFCC397B).toIntArray(),
    "Fuchsia Rose" to Color(0xFFC74375).toIntArray(),
    "Fulvous" to Color(0xFFE48400).toIntArray(),
    "Fuzzy Wuzzy" to Color(0xFF87421F).toIntArray(),

    "Gainsboro" to Color(0xFFDCDCDC).toIntArray(),
    "Gamboge" to Color(0xFFE49B0F).toIntArray(),
    "Garnet" to Color(0xFF733635).toIntArray(),
    "Generic Viridian" to Color(0xFF007F66).toIntArray(),
    "Ghost White" to Color(0xFFF8F8FF).toIntArray(),
    "Glaucous" to Color(0xFF6082B6).toIntArray(),
    "Glossy Grape" to Color(0xFFAB92B3).toIntArray(),
    "GO Green" to Color(0xFF00AB66).toIntArray(),
    "Gold (Metallic)" to Color(0xFFD4AF37).toIntArray(),
    "Gold (Web) (Golden)" to Color(0xFFFFD700).toIntArray(),
    "Gold (Crayola)" to Color(0xFFE6BE8A).toIntArray(),
    "Gold Fusion" to Color(0xFF85754E).toIntArray(),
    "Golden Brown" to Color(0xFF996515).toIntArray(),
    "Golden Poppy" to Color(0xFFFCC200).toIntArray(),
    "Golden Yellow" to Color(0xFFFFDF00).toIntArray(),
    "Goldenrod" to Color(0xFFDAA520).toIntArray(),
    "Gotham Green" to Color(0xFF00573F).toIntArray(),
    "Granite Gray" to Color(0xFF676767).toIntArray(),
    "Granny Smith Apple" to Color(0xFFA8E4A0).toIntArray(),
    "Gray (Web)" to Color(0xFF808080).toIntArray(),
    "Gray (X11 Gray)" to Color(0xFFBEBEBE).toIntArray(),
    "Green" to Color(0xFF00FF00).toIntArray(),
    "Green (Crayola)" to Color(0xFF1CAC78).toIntArray(),
    "Green (Web)" to Color(0xFF008000).toIntArray(),
    "Green (Munsell)" to Color(0xFF00A877).toIntArray(),
    "Green (NCS)" to Color(0xFF009F6B).toIntArray(),
    "Green (Pantone)" to Color(0xFF00AD43).toIntArray(),
    "Green (Pigment)" to Color(0xFF00A550).toIntArray(),
    "Green-Blue" to Color(0xFF1164B4).toIntArray(),
    "Green-Blue (Crayola)" to Color(0xFF2887C8).toIntArray(),
    "Green-Cyan" to Color(0xFF0095B6).toIntArray(),
    "Green-Yellow" to Color(0xFFADFF2F).toIntArray(),
    "Green-Yellow (Crayola)" to Color(0xFFF0E891).toIntArray(),
    "Green Lizard" to Color(0xFFA7F432).toIntArray(),
    "Green Sheen" to Color(0xFF6EAEA1).toIntArray(),
    "Grullo" to Color(0xFFA99A86).toIntArray(),
    "Gunmetal" to Color(0xFF2A3439).toIntArray(),
    "Han Blue" to Color(0xFF446CCF).toIntArray(),
    "Han Purple" to Color(0xFF5218FA).toIntArray(),
    "Hansa Yellow" to Color(0xFFE9D66B).toIntArray(),
    "Harlequin" to Color(0xFF3FFF00).toIntArray(),
    "Harvest Gold" to Color(0xFFDA9100).toIntArray(),
    "Heat Wave" to Color(0xFFFF7A00).toIntArray(),
    "Heliotrope" to Color(0xFFDF73FF).toIntArray(),
    "Heliotrope Gray" to Color(0xFFAA98A9).toIntArray(),
    "Hollywood Cerise" to Color(0xFFF400A1).toIntArray(),
    "Honeydew" to Color(0xFFF0FFF0).toIntArray(),
    "Honolulu Blue" to Color(0xFF006DB0).toIntArray(),
    "Hooker's Green" to Color(0xFF49796B).toIntArray(),
    "Hot Fuchsia" to Color(0xFFFF00C6).toIntArray(),
    "Hot Magenta" to Color(0xFFFF1DCE).toIntArray(),
    "Hot Pink" to Color(0xFFFF69B4).toIntArray(),
    "Hunter Green" to Color(0xFF355E3B).toIntArray(),
    "Iceberg" to Color(0xFF71A6D2).toIntArray(),
    "Icterine" to Color(0xFFFCF75E).toIntArray(),
    "Illuminating Emerald" to Color(0xFF319177).toIntArray(),
    "Imperial Red" to Color(0xFFED2939).toIntArray(),
    "Inchworm" to Color(0xFFB2EC5D).toIntArray(),
    "Independence" to Color(0xFF4C516D).toIntArray(),
    "India Green" to Color(0xFF138808).toIntArray(),
    "Indian Red" to Color(0xFFCD5C5C).toIntArray(),
    "Indian Yellow" to Color(0xFFE3A857).toIntArray(),
    "Indigo" to Color(0xFF6A5DFF).toIntArray(),
    "Indigo (Color Wheel)" to Color(0xFF4000FF).toIntArray(),
    "Indigo Dye" to Color(0xFF00416A).toIntArray(),
    "International Klein Blue" to Color(0xFF130A8F).toIntArray(),
    "International Orange (Aerospace)" to Color(0xFFFF4F00).toIntArray(),
    "International Orange (Engineering)" to Color(0xFFBA160C).toIntArray(),
    "International Orange (Golden Gate Bridge)" to Color(0xFFC0362C).toIntArray(),
    "Iris" to Color(0xFF5A4FCF).toIntArray(),
    "Irresistible" to Color(0xFFB3446C).toIntArray(),
    "Isabelline" to Color(0xFFF4F0EC).toIntArray(),
    "Italian Sky Blue" to Color(0xFFB2FFFF).toIntArray(),
    "Ivory" to Color(0xFFFFFFF0).toIntArray(),
    "Jade" to Color(0xFF00A86B).toIntArray(),
    "Japanese Carmine" to Color(0xFF9D2933).toIntArray(),
    "Japanese Violet" to Color(0xFF5B3256).toIntArray(),
    "Jasmine" to Color(0xFFF8DE7E).toIntArray(),
    "Jazzberry Jam" to Color(0xFFA50B5E).toIntArray(),
    "Jet" to Color(0xFF343434).toIntArray(),
    "Jonquil" to Color(0xFFF4CA16).toIntArray(),
    "June Bud" to Color(0xFFBDDA57).toIntArray(),
    "Jungle Green" to Color(0xFF29AB87).toIntArray(),
    "Kelly Green" to Color(0xFF4CBB17).toIntArray(),
    "Keppel" to Color(0xFF3AB09E).toIntArray(),
    "Key Lime" to Color(0xFFE8F48C).toIntArray(),
    "Khaki (Web)" to Color(0xFFC3B091).toIntArray(),
    "Khaki (X11)" to Color(0xFFF0E68C).toIntArray(),
    "Kobe" to Color(0xFF882D17).toIntArray(),
    "Kobi" to Color(0xFFE79FC4).toIntArray(),
    "Kobicha" to Color(0xFF6B4423).toIntArray(),
    "Kombu Green" to Color(0xFF354230).toIntArray(),
    "KSU Purple" to Color(0xFF512888).toIntArray(),
    "La Salle Green" to Color(0xFF087830).toIntArray(),
    "Languid Lavender" to Color(0xFFD6CADD).toIntArray(),
    "Lanzones" to Color(0xFFE0BC5B).toIntArray(),
    "Lapis Lazuli" to Color(0xFF26619C).toIntArray(),
    "Laser Lemon" to Color(0xFFFFFF66).toIntArray(),
    "Laurel Green" to Color(0xFFA9BA9D).toIntArray(),
    "Lava" to Color(0xFFCF1020).toIntArray(),
    "Lavender (Floral)" to Color(0xFFB57EDC).toIntArray(),
    "Lavender (Web)" to Color(0xFFE6E6FA).toIntArray(),
    "Lavender Blue" to Color(0xFFCCCCFF).toIntArray(),
    "Lavender Blush" to Color(0xFFFFF0F5).toIntArray(),
    "Lavender Gray" to Color(0xFFC4C3D0).toIntArray(),
    "Lavender Magenta" to Color(0xFFEE82EE).toIntArray(),
    "Lavender Mist" to Color(0xFFE6E6FA).toIntArray(),
    "Lavender Purple" to Color(0xFF967BB6).toIntArray(),
    "Lavender Rose" to Color(0xFFFBA0E3).toIntArray(),
    "Lawn Green" to Color(0xFF7CFC00).toIntArray(),
    "Lemon" to Color(0xFFFFFF00).toIntArray(),
    "Lemon Chiffon" to Color(0xFFFFFACD).toIntArray(),
    "Lemon Curry" to Color(0xFFCCA01D).toIntArray(),
    "Lemon Glacier" to Color(0xFFFDFF00).toIntArray(),
    "Lemon Iced Tea" to Color(0xFFBD3000).toIntArray(),
    "Lemon Lime Yellow" to Color(0xFFE3FF00).toIntArray(),
    "Lemon Lime Green" to Color(0xFF5CFF67).toIntArray(),
    "Lemon Meringue" to Color(0xFFF6EABE).toIntArray(),
    "Lemon Yellow" to Color(0xFFFFF44F).toIntArray(),
    "Lemon Yellow (Crayola)" to Color(0xFFFFFF9F).toIntArray(),
    "Liberty" to Color(0xFF545AA7).toIntArray(),
    "Licorice" to Color(0xFF1A1110).toIntArray(),
    "Light Blue" to Color(0xFFADD8E6).toIntArray(),
    "Light Brown" to Color(0xFFB5651D).toIntArray(),
    "Light Coral" to Color(0xFFF08080).toIntArray(),
    "Light Carmine Pink" to Color(0xFFE66771).toIntArray(),
    "Light Chocolate Cosmos" to Color(0xFF551F2F).toIntArray(),
    "Light Cobalt Blue" to Color(0xFF88ACE0).toIntArray(),
    "Light Cornflower Blue" to Color(0xFF93CCEA).toIntArray(),
    "Light Crimson" to Color(0xFFF56991).toIntArray(),
    "Light Cyan" to Color(0xFFE0FFFF).toIntArray(),
    "Light French Beige" to Color(0xFFC8AD7F).toIntArray(),
    "Light Fuchsia Pink" to Color(0xFFF984E4).toIntArray(),
    "Light Goldenrod Yellow" to Color(0xFFFAFAD2).toIntArray(),
    "Light Gold" to Color(0xFFB29700).toIntArray(),
    "Light Gray" to Color(0xFFD3D3D3).toIntArray(),
    "Light Grayish Magenta" to Color(0xFFCC99CC).toIntArray(),
    "Light Green" to Color(0xFF90EE90).toIntArray(),
    "Light Medium Orchid" to Color(0xFFD39BCB).toIntArray(),
    "Light Orange" to Color(0xFFFED8B1).toIntArray(),
    "Light Orchid" to Color(0xFFE6A8D7).toIntArray(),
    "Light Pastel Purple" to Color(0xFFB19CD9).toIntArray(),
    "Light Periwinkle" to Color(0xFFC5CBE1).toIntArray(),
    "Light Red" to Color(0xFFFFCCCB).toIntArray(),
    "Light Red Ochre" to Color(0xFFE97451).toIntArray(),
    "Light Pink" to Color(0xFFFFB6C1).toIntArray(),
    "Light Salmon" to Color(0xFFFFA07A).toIntArray(),
    "Light Salmon Pink" to Color(0xFFFF9999).toIntArray(),
    "Light Sea Green" to Color(0xFF20B2AA).toIntArray(),
    "Light Silver" to Color(0xFFD8D8D8).toIntArray(),
    "Light Thulian Pink" to Color(0xFFE68FAC).toIntArray(),
    "Light Sky Blue" to Color(0xFF87CEFA).toIntArray(),
    "Light Slate Gray" to Color(0xFF778899).toIntArray(),
    "Light Steel Blue" to Color(0xFFB0C4DE).toIntArray(),
    "Light Yellow" to Color(0xFFFFFFE0).toIntArray(),
    "Lilac" to Color(0xFFC8A2C8).toIntArray(),
    "Lilac Luster" to Color(0xFFAE98AA).toIntArray(),
    "Lime (Color Wheel)" to Color(0xFFBFFF00).toIntArray(),
    "Lime (Web)" to Color(0xFF00FF00).toIntArray(),
    "Lime Green" to Color(0xFF32CD32).toIntArray(),
    "Limerick" to Color(0xFF9DC209).toIntArray(),
    "Lincoln Green" to Color(0xFF195905).toIntArray(),
    "Linen" to Color(0xFFFAF0E6).toIntArray(),
    "Lion" to Color(0xFFDECC9C).toIntArray(),
    "Liseran Purple" to Color(0xFFDE6FA1).toIntArray(),
    "Little Boy Blue" to Color(0xFF6CA0DC).toIntArray(),
    "Little Girl Pink" to Color(0xFFF8B9D4).toIntArray(),
    "Liver" to Color(0xFF674C47).toIntArray(),
    "Liver (Dogs)" to Color(0xFFB86D29).toIntArray(),
    "Liver (Organ)" to Color(0xFF6C2E1F).toIntArray(),
    "Liver Chestnut" to Color(0xFF987456).toIntArray(),
    "Livid" to Color(0xFF6699CC).toIntArray(),
    "Lotion" to Color(0xFFFEFDFA).toIntArray(),
    "Lotion Blue" to Color(0xFF15F2FD).toIntArray(),
    "Lotion Pink" to Color(0xFFECCFCF).toIntArray(),
    "Lumber" to Color(0xFFFFE4CD).toIntArray(),
    "Lust" to Color(0xFFE62020).toIntArray(),
    "Maastricht Blue" to Color(0xFF001C3D).toIntArray(),
    "Macaroni and Cheese" to Color(0xFFFFBD88).toIntArray(),
    "Madder Lake" to Color(0xFFCC3336).toIntArray(),
    "Magenta" to Color(0xFFFF00FF).toIntArray(),
    "Magenta (Crayola)" to Color(0xFFF653A6).toIntArray(),
    "Magenta (Dye)" to Color(0xFFCA1F7B).toIntArray(),
    "Magenta (Pantone)" to Color(0xFFD0417E).toIntArray(),
    "Magenta Haze" to Color(0xFF9F4576).toIntArray(),
    "Magic Mint" to Color(0xFFAAF0D1).toIntArray(),
    "Magnolia" to Color(0xFFF8F4FF).toIntArray(),
    "Mahogany" to Color(0xFFC04000).toIntArray(),
    "Maize" to Color(0xFFFBEC5D).toIntArray(),
    "Maize (Crayola)" to Color(0xFFF2C649).toIntArray(),
    "Majorelle Blue" to Color(0xFF6050DC).toIntArray(),
    "Malachite" to Color(0xFF0BDA51).toIntArray(),
    "Manatee" to Color(0xFF979AAA).toIntArray(),
    "Mandarin" to Color(0xFFF37A48).toIntArray(),
    "Mango" to Color(0xFFF4BB44).toIntArray(),
    "Mango Green" to Color(0xFF96FF00).toIntArray(),
    "Mango Tango" to Color(0xFFFF8243).toIntArray(),
    "Mantis" to Color(0xFF74C365).toIntArray(),
    "Mardi Gras" to Color(0xFF880085).toIntArray(),
    "Marigold" to Color(0xFFEAA221).toIntArray(),
    "Maroon (Crayola)" to Color(0xFFC32148).toIntArray(),
    "Maroon (HTML/CSS)" to Color(0xFF800000).toIntArray(),
    "Maroon (X11)" to Color(0xFFB03060).toIntArray(),
    "Mauve" to Color(0xFFE0B0FF).toIntArray(),
    "Mauve Taupe" to Color(0xFF915F6D).toIntArray(),
    "Mauvelous" to Color(0xFFEF98AA).toIntArray(),
    "Maximum Blue" to Color(0xFF47ABCC).toIntArray(),
    "Maximum Blue Green" to Color(0xFF30BFBF).toIntArray(),
    "Maximum Blue Purple" to Color(0xFFACACE6).toIntArray(),
    "Maximum Green" to Color(0xFF5E8C31).toIntArray(),
    "Maximum Green Yellow" to Color(0xFFD9E650).toIntArray(),
    "Maximum Orange" to Color(0xFFFF5B00).toIntArray(),
    "Maximum Purple" to Color(0xFF733380).toIntArray(),
    "Maximum Pink" to Color(0xFFF6A5F2).toIntArray(),
    "Maximum Red" to Color(0xFFD92121).toIntArray(),
    "Maximum Red Purple" to Color(0xFFA63A79).toIntArray(),
    "Maximum Violet" to Color(0xFF892F77).toIntArray(),
    "Maximum Yellow" to Color(0xFFFAFA37).toIntArray(),
    "Maximum Yellow Red" to Color(0xFFF2BA49).toIntArray(),
    "May Green" to Color(0xFF4C9141).toIntArray(),
    "Maya Blue" to Color(0xFF73C2FB).toIntArray(),
    "Medium Aquamarine" to Color(0xFF66DDAA).toIntArray(),
    "Medium Blue" to Color(0xFF0000CD).toIntArray(),
    "Medium Candy Apple Red" to Color(0xFFE2062C).toIntArray(),
    "Medium Carmine" to Color(0xFFAF4035).toIntArray(),
    "Medium Champagne" to Color(0xFFF3E5AB).toIntArray(),
    "Medium Green" to Color(0xFF037949).toIntArray(),
    "Medium Electric Blue" to Color(0xFF035096).toIntArray(),
    "Medium Jungle Green" to Color(0xFF1C352D).toIntArray(),
    "Medium Lavender Magenta" to Color(0xFFDDA0DD).toIntArray(),
    "Medium Orange" to Color(0xFFFF7802).toIntArray(),
    "Medium Orchid" to Color(0xFFBA55D3).toIntArray(),
    "Medium Persimmon" to Color(0xFFEC5800).toIntArray(),
    "Medium Persian Blue" to Color(0xFF0067A5).toIntArray(),
    "Medium Pink" to Color(0xFFFE6E9F).toIntArray(),
    "Medium Purple" to Color(0xFF9370DB).toIntArray(),
    "Medium Red" to Color(0xFFB10304).toIntArray(),
    "Medium Red-Violet" to Color(0xFFBB3385).toIntArray(),
    "Medium Ruby" to Color(0xFFAA4069).toIntArray(),
    "Medium Sea Green" to Color(0xFF3CB371).toIntArray(),
    "Medium Sky Blue" to Color(0xFF80DAEB).toIntArray(),
    "Medium Slate Blue" to Color(0xFF7B68EE).toIntArray(),
    "Medium Spring Bud" to Color(0xFFC9DC87).toIntArray(),
    "Medium Spring Green" to Color(0xFF00FA9A).toIntArray(),
    "Medium Taupe" to Color(0xFF674C47).toIntArray(),
    "Medium Turquoise" to Color(0xFF48D1CC).toIntArray(),
    "Medium Tuscan Red" to Color(0xFF79443B).toIntArray(),
    "Medium Vermilion" to Color(0xFFD9603B).toIntArray(),
    "Medium Violet" to Color(0xFF65315F).toIntArray(),
    "Medium Violet-Red" to Color(0xFFC71585).toIntArray(),
    "Medium Yellow" to Color(0xFFFFE302).toIntArray(),
    "Mellow Apricot" to Color(0xFFF8B878).toIntArray(),
    "Mellow Yellow" to Color(0xFFF8DE7E).toIntArray(),
    "Melon" to Color(0xFFFDBCB4).toIntArray(),
    "Melon (Crayola)" to Color(0xFFFEBAAD).toIntArray(),
    "Menthol" to Color(0xFFC1F9A2).toIntArray(),
    "Metallic Blue" to Color(0xFF32527B).toIntArray(),
    "Metallic Bronze" to Color(0xFFA97142).toIntArray(),
    "Metallic Brown" to Color(0xFFAC4313).toIntArray(),
    "Metallic Gold" to Color(0xFFD3AF37).toIntArray(),
    "Metallic Green" to Color(0xFF296E01).toIntArray(),
    "Metallic Orange" to Color(0xFFDA680F).toIntArray(),
    "Metallic Pink" to Color(0xFFEDA6C4).toIntArray(),
    "Metallic Red" to Color(0xFFA62C2B).toIntArray(),
    "Metallic Seaweed" to Color(0xFF0A7E8C).toIntArray(),
    "Metallic Silver" to Color(0xFFA8A9AD).toIntArray(),
    "Metallic Sunburst" to Color(0xFF9C7C38).toIntArray(),
    "Metallic Violet" to Color(0xFF5B0A91).toIntArray(),
    "Metallic Yellow" to Color(0xFFFDCC0D).toIntArray(),
    "Mexican Pink" to Color(0xFFE4007C).toIntArray(),
    "Microsoft Blue" to Color(0xFF00A2ED).toIntArray(),
    "Microsoft Edge Blue" to Color(0xFF0078D7).toIntArray(),
    "Microsoft Green" to Color(0xFF7DB700).toIntArray(),
    "Microsoft Red" to Color(0xFFF04E1F).toIntArray(),
    "Microsoft Yellow" to Color(0xFFFDB900).toIntArray(),
    "Middle Blue" to Color(0xFF7ED4E6).toIntArray(),
    "Middle Blue Green" to Color(0xFF8DD9CC).toIntArray(),
    "Middle Blue Purple" to Color(0xFF8B72BE).toIntArray(),
    "Middle Grey" to Color(0xFF8BB680).toIntArray(),
    "Middle Green" to Color(0xFF4D8C57).toIntArray(),
    "Middle Green Yellow" to Color(0xFFACBF60).toIntArray(),
    "Middle Purple" to Color(0xFFD982B5).toIntArray(),
    "Middle Red" to Color(0xFFE58E73).toIntArray(),
    "Middle Red Purple" to Color(0xFFA55353).toIntArray(),
    "Middle Yellow" to Color(0xFFFFEB00).toIntArray(),
    "Middle Yellow Red" to Color(0xFFECB176).toIntArray(),
    "Midnight" to Color(0xFF702670).toIntArray(),
    "Midnight Blue" to Color(0xFF191970).toIntArray(),
    "Midnight Green (Eagle Green)" to Color(0xFF004953).toIntArray(),
    "Mikado Yellow" to Color(0xFFFFC40C).toIntArray(),
    "Milk" to Color(0xFFFFFEF1).toIntArray(),
    "Milk Chocolate" to Color(0xFF84563C).toIntArray(),
    "Mimi Pink" to Color(0xFFFFDAE9).toIntArray(),
    "Mindaro" to Color(0xFFE3F988).toIntArray(),
    "Ming" to Color(0xFF36747D).toIntArray(),
    "Minion Yellow" to Color(0xFFF5E050).toIntArray(),
    "Mint" to Color(0xFF3EB489).toIntArray(),
    "Mint Cream" to Color(0xFFF5FFFA).toIntArray(),
    "Mint Green" to Color(0xFF98FF98).toIntArray(),
    "Misty Moss" to Color(0xFFBBB477).toIntArray(),
    "Misty Rose" to Color(0xFFFFE4E1).toIntArray(),
    "Moccasin" to Color(0xFFFFE4B5).toIntArray(),
    "Mocha" to Color(0xFFBEA493).toIntArray(),
    "Mode Beige" to Color(0xFF967117).toIntArray(),
    "Moonstone" to Color(0xFF3AA8C1).toIntArray(),
    "Moonstone Blue" to Color(0xFF73A9C2).toIntArray(),
    "Mona Lisa" to Color(0xFFFF948E).toIntArray(),
    "Mordant Red 19" to Color(0xFFAE0C00).toIntArray(),
    "Morning Blue" to Color(0xFF8DA399).toIntArray(),
    "Moss Green" to Color(0xFF8A9A5B).toIntArray(),
    "Mountain Meadow" to Color(0xFF30BA8F).toIntArray(),
    "Mountbatten Pink" to Color(0xFF997A8D).toIntArray(),
    "MSU Green" to Color(0xFF18453B).toIntArray(),
    "Mud" to Color(0xFF70543E).toIntArray(),
    "Mughal Green" to Color(0xFF306030).toIntArray(),
    "Mulberry" to Color(0xFFC54B8C).toIntArray(),
    "Mulberry (Crayola)" to Color(0xFFC8509B).toIntArray(),
    "Mummy's Tomb" to Color(0xFF828E84).toIntArray(),
    "Mustard" to Color(0xFFFFDB58).toIntArray(),
    "Mustard Brown" to Color(0xFFCD7A00).toIntArray(),
    "Mustard Green" to Color(0xFF6E6E30).toIntArray(),
    "Mustard Yellow" to Color(0xFFE1AD01).toIntArray(),
    "Myrtle Green" to Color(0xFF317873).toIntArray(),
    "Mystic" to Color(0xFFD65282).toIntArray(),
    "Mystic Maroon" to Color(0xFFAD4379).toIntArray(),
    "Nadeshiko Pink" to Color(0xFFF6ADC6).toIntArray(),
    "Napier Green" to Color(0xFF2A8000).toIntArray(),
    "Naples Yellow" to Color(0xFFFADA5E).toIntArray(),
    "Navajo White" to Color(0xFFFFDEAD).toIntArray(),
    "Navy Blue" to Color(0xFF000080).toIntArray(),
    "Navy Blue (Crayola)" to Color(0xFF1974D2).toIntArray(),
    "Navy Purple" to Color(0xFF9457EB).toIntArray(),
    "Neon Blue" to Color(0xFF4666FF).toIntArray(),
    "Neon Brown" to Color(0xFFC3732A).toIntArray(),
    "Neon Carrot" to Color(0xFFAAF0D1).toIntArray(),
    "Neon Cyan" to Color(0xFF00FEFC).toIntArray(),
    "Neon Fuchsia" to Color(0xFFFE4164).toIntArray(),
    "Neon Gold" to Color(0xFFCFAA01).toIntArray(),
    "Neon Gray" to Color(0xFF808080).toIntArray(),
    "Neon Dark Green" to Color(0xFF008443).toIntArray(),
    "Neon Green" to Color(0xFF39FF14).toIntArray(),
    "Neon Pink" to Color(0xFFFE34FE).toIntArray(),
    "Neon Purple" to Color(0xFF9457EB).toIntArray(),
    "Neon Red" to Color(0xFFFF1818).toIntArray(),
    "Neon Scarlet" to Color(0xFFFF2603).toIntArray(),
    "Neon Silver" to Color(0xFFCCCCCC).toIntArray(),
    "Neon Tangerine" to Color(0xFFF6890A).toIntArray(),
    "Neon Yellow" to Color(0xFFFFF700).toIntArray(),
    "New Car" to Color(0xFF214FC6).toIntArray(),
    "New York Pink" to Color(0xFFD7837F).toIntArray(),
    "Nickel" to Color(0xFF727472).toIntArray(),
    "Nintendo Red" to Color(0xFFE4000F).toIntArray(),
    "Non-Photo Blue" to Color(0xFFA4DDED).toIntArray(),
    "Nyanza" to Color(0xFFE9FFDB).toIntArray(),
    "Ocean Boat Blue" to Color(0xFF0077BE).toIntArray(),
    "Ochre" to Color(0xFFCC7722).toIntArray(),
    "Office Green" to Color(0xFF008000).toIntArray(),
    "Old Burgundy" to Color(0xFF43302E).toIntArray(),
    "Old Gold" to Color(0xFFCFB53B).toIntArray(),
    "Old Heliotrope" to Color(0xFF563C5C).toIntArray(),
    "Old Lace" to Color(0xFFFDF5E6).toIntArray(),
    "Old Lavender" to Color(0xFF796878).toIntArray(),
    "Old Mauve" to Color(0xFF673147).toIntArray(),
    "Old Moss Green" to Color(0xFF867E36).toIntArray(),
    "Old Rose" to Color(0xFFC08081).toIntArray(),
    "Old Silver" to Color(0xFF848482).toIntArray(),
    "Olive" to Color(0xFF808000).toIntArray(),
    "Olive Drab (#3)" to Color(0xFF6B8E23).toIntArray(),
    "Olive Drab #7" to Color(0xFF3C341F).toIntArray(),
    "Olive Green" to Color(0xFFB5B35C).toIntArray(),
    "Olivine" to Color(0xFF9AB973).toIntArray(),
    "Onyx" to Color(0xFF353839).toIntArray(),
    "Opal" to Color(0xFFA8C3BC).toIntArray(),
    "Opera Mauve" to Color(0xFFB784A7).toIntArray(),
    "Orange" to Color(0xFFFF7F00).toIntArray(),
    "Orange (Crayola)" to Color(0xFFFF7538).toIntArray(),
    "Orange (Pantone)" to Color(0xFFFF5800).toIntArray(),
    "Orange (Web)" to Color(0xFFFFA500).toIntArray(),
    "Orange Peel" to Color(0xFFFF9F00).toIntArray(),
    "Orange-Red" to Color(0xFFFF681F).toIntArray(),
    "Orange-Red (Crayola)" to Color(0xFFFF5349).toIntArray(),
    "Orange Soda" to Color(0xFFFA5B3D).toIntArray(),
    "Orange-Yellow" to Color(0xFFF5BD1F).toIntArray(),
    "Orange-Yellow (Crayola)" to Color(0xFFF8D568).toIntArray(),
    "Orchid" to Color(0xFFDA70D6).toIntArray(),
    "Orchid Pink" to Color(0xFFF2BDCD).toIntArray(),
    "Orchid (Crayola)" to Color(0xFFE29CD2).toIntArray(),
    "Outer Space (Crayola)" to Color(0xFF2D383A).toIntArray(),
    "Outrageous Orange" to Color(0xFFFF6E4A).toIntArray(),
    "Oxblood" to Color(0xFF4A0000).toIntArray(),
    "Oxford Blue" to Color(0xFF002147).toIntArray(),
    "OU Crimson Red" to Color(0xFF841617).toIntArray(),
    "Pacific Blue" to Color(0xFF1CA9C9).toIntArray(),
    "Pakistan Green" to Color(0xFF006600).toIntArray(),
    "Palatinate Purple" to Color(0xFF682860).toIntArray(),
    "Pale Aqua" to Color(0xFFBED3E5).toIntArray(),
    "Pale Brown" to Color(0xFF987654).toIntArray(),
    "Pale Carmine" to Color(0xFFAF4035).toIntArray(),
    "Pale Cerulean" to Color(0xFF9BC4E2).toIntArray(),
    "Pale Chestnut" to Color(0xFFDDADAF).toIntArray(),
    "Pale Copper" to Color(0xFFDA8A67).toIntArray(),
    "Pale Cornflower Blue" to Color(0xFFABCDEF).toIntArray(),
    "Pale Cyan" to Color(0xFF87D3F8).toIntArray(),
    "Pale Gold" to Color(0xFFE6BE8A).toIntArray(),
    "Pale Goldenrod" to Color(0xFFEEE8AA).toIntArray(),
    "Pale Green" to Color(0xFF98FB98).toIntArray(),
    "Pale Lavender" to Color(0xFFDCD0FF).toIntArray(),
    "Pale Magenta" to Color(0xFFF984E5).toIntArray(),
    "Pale Pink" to Color(0xFFFADADD).toIntArray(),
    "Pale Plum" to Color(0xFFDDA0DD).toIntArray(),
    "Pale Robin Egg Blue" to Color(0xFF96DED1).toIntArray(),
    "Pale Silver" to Color(0xFFC9C0BB).toIntArray(),
    "Pale Spring Bud" to Color(0xFFECEBBD).toIntArray(),
    "Pale Taupe" to Color(0xFFBC987E).toIntArray(),
    "Pale Turquoise" to Color(0xFFAFEEEE).toIntArray(),
    "Pale Violet" to Color(0xFFCC99FF).toIntArray(),
    "Pale Violet-Red" to Color(0xFFDB7093).toIntArray(),
    "Pale Dogwood" to Color(0xFFED7A9B).toIntArray(),
    "Pale Purple (Pantone)" to Color(0xFFFAE6FA).toIntArray(),
    "Pansy Purple" to Color(0xFF78184A).toIntArray(),
    "Paolo Veronese Green" to Color(0xFF009B7D).toIntArray(),
    "Papaya Whip" to Color(0xFFFFEFD5).toIntArray(),
    "Paradise Pink" to Color(0xFFE63E62).toIntArray(),
    "Parchment" to Color(0xFFF1E9D2).toIntArray(),
    "Paris Green" to Color(0xFF50C878).toIntArray(),
    "Pastel Blue" to Color(0xFFAEC6CF).toIntArray(),
    "Pastel Brown" to Color(0xFF836953).toIntArray(),
    "Pastel Gray" to Color(0xFFCFCFC4).toIntArray(),
    "Pastel Green" to Color(0xFF77DD77).toIntArray(),
    "Pastel Magenta" to Color(0xFFF49AC2).toIntArray(),
    "Pastel Orange" to Color(0xFFFFB347).toIntArray(),
    "Pastel Pink" to Color(0xFFDEA5A4).toIntArray(),
    "Pastel Purple" to Color(0xFFB39EB5).toIntArray(),
    "Pastel Red" to Color(0xFFFF6961).toIntArray(),
    "Pastel Violet" to Color(0xFFCB99C9).toIntArray(),
    "Pastel Yellow" to Color(0xFFFDFD96).toIntArray(),
    "Patriarch" to Color(0xFF800080).toIntArray(),
    "Paua" to Color(0xFF1F005E).toIntArray(),
    "Payne's Grey" to Color(0xFF536878).toIntArray(),
    "Peach" to Color(0xFFFFE5B4).toIntArray(),
    "Peach (Crayola)" to Color(0xFFFFCBA4).toIntArray(),
    "Peach Puff" to Color(0xFFFFDAB9).toIntArray(),
    "Pear" to Color(0xFFD1E231).toIntArray(),
    "Pearl" to Color(0xFFEAE0C8).toIntArray(),
    "Pearly Purple" to Color(0xFFB768A2).toIntArray(),
    "Peridot" to Color(0xFFE6E200).toIntArray(),
    "Periwinkle" to Color(0xFFCCCCFF).toIntArray(),
    "Periwinkle (Crayola)" to Color(0xFFC3CDE6).toIntArray(),
    "Permanent Geranium Lake" to Color(0xFFE12C2C).toIntArray(),
    "Persian Blue" to Color(0xFF1C39BB).toIntArray(),
    "Persian Green" to Color(0xFF00A693).toIntArray(),
    "Persian Indigo" to Color(0xFF32127A).toIntArray(),
    "Persian Orange" to Color(0xFFD99058).toIntArray(),
    "Persian Pink" to Color(0xFFF77FBE).toIntArray(),
    "Persian Plum" to Color(0xFF701C1C).toIntArray(),
    "Persian Red" to Color(0xFFCC3333).toIntArray(),
    "Persian Rose" to Color(0xFFFE28A2).toIntArray(),
    "Persimmon" to Color(0xFFEC5800).toIntArray(),
    "Peru" to Color(0xFFCD853F).toIntArray(),
    "Petal" to Color(0xFFF5E2E2).toIntArray(),
    "Pewter Blue" to Color(0xFF8BA8B7).toIntArray(),
    "Phlox" to Color(0xFFDF00FF).toIntArray(),
    "Phthalo Blue" to Color(0xFF000F89).toIntArray(),
    "Phthalo Green" to Color(0xFF123524).toIntArray(),
    "Picotee Blue" to Color(0xFF2E2787).toIntArray(),
    "Pictorial Carmine" to Color(0xFFC30B4E).toIntArray(),
    "Piggy Pink" to Color(0xFFFDDDE6).toIntArray(),
    "Pine Green" to Color(0xFF01796F).toIntArray(),
    "Pine Green (Alternate)" to Color(0xFF2A2F23).toIntArray(),
    "Pink" to Color(0xFFFFC0CB).toIntArray(),
    "Pink (Pantone)" to Color(0xFFD74894).toIntArray(),
    "Pink Diamond" to Color(0xFFF6D6DE).toIntArray(),
    "Pink Flamingo" to Color(0xFFFC74FD).toIntArray(),
    "Pink Lace" to Color(0xFFFFDDF4).toIntArray(),
    "Pink Lavender" to Color(0xFFD8B2D1).toIntArray(),
    "Pink Pearl" to Color(0xFFE7ACCF).toIntArray(),
    "Pink Sherbet" to Color(0xFFF78FA7).toIntArray(),
    "Pistachio" to Color(0xFF93C572).toIntArray(),
    "Platinum" to Color(0xFFE5E4E2).toIntArray(),
    "Plum" to Color(0xFF8E4585).toIntArray(),
    "Plum (Web)" to Color(0xFFDD0ADD).toIntArray(),
    "Plump Purple" to Color(0xFF5946B2).toIntArray(),
    "Poison Purple" to Color(0xFF7F01FE).toIntArray(),
    "Police Blue" to Color(0xFF374F6B).toIntArray(),
    "Polished Pine" to Color(0xFF5DA493).toIntArray(),
    "Pomp and Power" to Color(0xFF86608E).toIntArray(),
    "Popstar" to Color(0xFFBE4F62).toIntArray(),
    "Portland Orange" to Color(0xFFFF5A36).toIntArray(),
    "Powder Blue" to Color(0xFFB0E0E6).toIntArray(),
    "Prairie Gold" to Color(0xFFE1CA7A).toIntArray(),
    "Princeton Orange" to Color(0xFFF58025).toIntArray(),
    "Prune" to Color(0xFF701C1C).toIntArray(),
    "Prussian Blue" to Color(0xFF003153).toIntArray(),
    "Psychedelic Purple" to Color(0xFFDF00FF).toIntArray(),
    "Puce" to Color(0xFFCC8899).toIntArray(),
    "Puce Red" to Color(0xFFCC8899).toIntArray(),
    "Pullman Brown (UPS Brown)" to Color(0xFF644117).toIntArray(),
    "Pumpkin" to Color(0xFFFF7518).toIntArray(),
    "Purple" to Color(0xFF6A0DAD).toIntArray(),
    "Purple (Web)" to Color(0xFF800080).toIntArray(),
    "Purple (Munsell)" to Color(0xFF9F00C5).toIntArray(),
    "Purple (X11)" to Color(0xFFA020F0).toIntArray(),
    "Purple Mountain Majesty" to Color(0xFF9678B6).toIntArray(),
    "Purple Navy" to Color(0xFF4E5180).toIntArray(),
    "Purple Pizzazz" to Color(0xFFFE4D8C).toIntArray(),
    "Purple Plum" to Color(0xFF9C51B6).toIntArray(),
    "Purpureus" to Color(0xFF9A4EAE).toIntArray(),
    "Purple Taupe" to Color(0xFF50404D).toIntArray(),
    "Quartz" to Color(0xFF54184F).toIntArray(),
    "Queen Blue" to Color(0xFF436B95).toIntArray(),
    "Queen Pink" to Color(0xFFE8CCD7).toIntArray(),
    "Quick Silver" to Color(0xFFA6A6A6).toIntArray(),
    "Quinacridone Magenta" to Color(0xFF8E3A59).toIntArray(),
    "Quincy" to Color(0xFF6A5445).toIntArray(),
    "Radical Red" to Color(0xFFFF355E).toIntArray(),
    "Raisin Black" to Color(0xFF242124).toIntArray(),
    "Rajah" to Color(0xFFFBAB60).toIntArray(),
    "Raspberry" to Color(0xFFE30B5D).toIntArray(),
    "Raspberry Glacé" to Color(0xFF915F6D).toIntArray(),
    "Raspberry Rose" to Color(0xFFB3446C).toIntArray(),
    "Raw Sienna" to Color(0xFFD68A59).toIntArray(),
    "Raw Umber" to Color(0xFF826644).toIntArray(),
    "Razzle Dazzle Rose" to Color(0xFFFF33CC).toIntArray(),
    "Razzmic Berry" to Color(0xFF8D4E85).toIntArray(),
    "Rebecca Purple" to Color(0xFF663399).toIntArray(),
    "Razzmatazz" to Color(0xFFE3256B).toIntArray(),
    "Red" to Color(0xFFFF0000).toIntArray(),
    "Red (Crayola)" to Color(0xFFFF434C).toIntArray(),
    "Red (Munsell)" to Color(0xFFEC1C24).toIntArray(),
    "Red (NCS)" to Color(0xFFC40233).toIntArray(),
    "Red (RYB)" to Color(0xFFFE2712).toIntArray(),
    "Red (Pantone)" to Color(0xFFDA291C).toIntArray(),
    "Red (Pigment)" to Color(0xFFED1C24).toIntArray(),
    "Red (Web)" to Color(0xFFFF0000).toIntArray(),
    "Red Salsa" to Color(0xFFFD3A4A).toIntArray(),
    "Red-Orange" to Color(0xFFFF5349).toIntArray(),
    "Red-Orange (Crayola)" to Color(0xFFFF4F00).toIntArray(),
    "Red-Orange (Pantone)" to Color(0xFFD0385A).toIntArray(),
    "Red-Purple" to Color(0xFFE4002B).toIntArray(),
    "Red-Violet" to Color(0xFFC71585).toIntArray(),
    "Reddish Brown" to Color(0xFF3D0A02).toIntArray(),
    "Rich Black" to Color(0xFF004040).toIntArray(),
    "Rich Brilliant Lavender" to Color(0xFFD3A6D4).toIntArray(),
    "Rich Candy Apple Red" to Color(0xFFE4002B).toIntArray(),
    "Rich Cantaloupe" to Color(0xFFE67F2F).toIntArray(),
    "Rich Lavender" to Color(0xFFE6C6D5).toIntArray(),
    "Rich Lilac" to Color(0xFF8A2D77).toIntArray(),
    "Rich Maroon" to Color(0xFF3D0A02).toIntArray(),
    "Rich Pink" to Color(0xFFFC0FC0).toIntArray(),
    "Rich Purple" to Color(0xFF4A0072).toIntArray(),
    "Rifle Green" to Color(0xFF414C54).toIntArray(),
    "Robin Egg Blue" to Color(0xFF00CCCC).toIntArray(),
    "Rocket Metallic" to Color(0xFF8A7F80).toIntArray(),
    "Rojo Spanish Red" to Color(0xFFA91101).toIntArray(),
    "Roman Silver" to Color(0xFF838996).toIntArray(),
    "Rose" to Color(0xFFFF007F).toIntArray(),
    "Rose Bonbon" to Color(0xFFF9429E).toIntArray(),
    "Rose Dust" to Color(0xFF9E6F6F).toIntArray(),
    "Rose Ebony" to Color(0xFF674846).toIntArray(),
    "Rose Gold" to Color(0xFFB76E79).toIntArray(),
    "Rose Madder" to Color(0xFFE06F8C).toIntArray(),
    "Rose Pink" to Color(0xFFFF66CC).toIntArray(),
    "Rose Red" to Color(0xFFBC1F1F).toIntArray(),
    "Rosewood" to Color(0xFF65000B).toIntArray(),
    "Rose Pompadour" to Color(0xFFED7A9B).toIntArray(),
    "Rose Taupe" to Color(0xFF905D5D).toIntArray(),
    "Rose Vale" to Color(0xFFAB4E52).toIntArray(),
    "Rosso Corsa" to Color(0xFFD40000).toIntArray(),
    "Rosy Brown" to Color(0xFFBC8F8F).toIntArray(),
    "Royal Blue (Dark)" to Color(0xFF002366).toIntArray(),
    "Royal Blue (Light)" to Color(0xFF4169E1).toIntArray(),
    "Royal Purple" to Color(0xFF7851A9).toIntArray(),
    "Royal Yellow" to Color(0xFFFFCC00).toIntArray(),
    "Ruber" to Color(0xFFD04A4A).toIntArray(),
    "Rubine Red" to Color(0xFFC810F0).toIntArray(),
    "Ruby" to Color(0xFFC8102E).toIntArray(),
    "Rufous" to Color(0xFF8C3F2E).toIntArray(),
    "Russet" to Color(0xFF80461B).toIntArray(),
    "Russians Green" to Color(0xFF9B9E9F).toIntArray(),
    "Russian Violet" to Color(0xFF32174D).toIntArray(),
    "Rust" to Color(0xFFB7410E).toIntArray(),
    "Rusty Red" to Color(0xFFDA5B4C).toIntArray(),
    "Sacramento State Green" to Color(0xFF003B49).toIntArray(),
    "Saddle Brown" to Color(0xFF8B4513).toIntArray(),
    "Safety Orange" to Color(0xFFFF7800).toIntArray(),
    "Safety Orange (Blaze Orange)" to Color(0xFFFF6700).toIntArray(),
    "Safety Yellow" to Color(0xFFEED202).toIntArray(),
    "Saffron" to Color(0xFFF4C430).toIntArray(),
    "Sage" to Color(0xFFBCB88A).toIntArray(),
    "St. Patrick's Blue" to Color(0xFF23297A).toIntArray(),
    "Salmon" to Color(0xFFFA8072).toIntArray(),
    "Salmon Pink" to Color(0xFFFF91A4).toIntArray(),
    "Sand" to Color(0xFFD2B48C).toIntArray(),
    "Sand Dune" to Color(0xFF9E6F4A).toIntArray(),
    "Sandy Brown" to Color(0xFFF4A460).toIntArray(),
    "Sandy Taupe" to Color(0xFF967117).toIntArray(),
    "Sap Green" to Color(0xFF507D2A).toIntArray(),
    "Sapphire" to Color(0xFF0F52BA).toIntArray(),
    "Sapphire (Crayola)" to Color(0xFF2D5DA1).toIntArray(),
    "Sapphire Blue" to Color(0xFF0F52BA).toIntArray(),
    "Satin Sheen Gold" to Color(0xFFC5B358).toIntArray(),
    "Scarlet" to Color(0xFFFF2400).toIntArray(),
    "Schauss Pink" to Color(0xFFFF91AF).toIntArray(),
    "School Bus Yellow" to Color(0xFFF4C300).toIntArray(),
    "Screamin' Green" to Color(0xFF66FF66).toIntArray(),
    "Sea Blue" to Color(0xFF006994).toIntArray(),
    "Sea Green" to Color(0xFF2E8B57).toIntArray(),
    "Sea Shell" to Color(0xFFFFF5EE).toIntArray(),
    "Shadow" to Color(0xFF8A795D).toIntArray(),
    "Shampoo" to Color(0xFFFFF1F2).toIntArray(),
    "Seal brown" to Color(0xFF59260B).toIntArray(),
    "Seashell" to Color(0xFFFFF5EE).toIntArray(),
    "Secret" to Color(0xFF764374).toIntArray(),
    "Selective yellow" to Color(0xFFFFBA00).toIntArray(),
    "Sepia" to Color(0xFF704214).toIntArray(),
    "Shadow" to Color(0xFF8A795D).toIntArray(),
    "Shadow blue" to Color(0xFF778BA5).toIntArray(),
    "Sheen green" to Color(0xFF8FD400).toIntArray(),
    "Shimmering Blush" to Color(0xFFD98695).toIntArray(),
    "Shiny Shamrock" to Color(0xFF5FA778).toIntArray(),
    "Shamrock Green" to Color(0xFF009E49).toIntArray(),
    "Shocking Pink" to Color(0xFFF7A8B8).toIntArray(),
    "Shocking Pink (Crayola)" to Color(0xFFFF6FFF).toIntArray(),
    "Sienna" to Color(0xFFA0522D).toIntArray(),
    "Silver" to Color(0xFFC0C0C0).toIntArray(),
    "Silver (Crayola)" to Color(0xFFC9C0BB).toIntArray(),
    "Silver (Metallic)" to Color(0xFFAAA9AD).toIntArray(),
    "Silver Chalice" to Color(0xFF9D9A9E).toIntArray(),
    "Silver Pink" to Color(0xFFC4AEAD).toIntArray(),
    "Silver Sand" to Color(0xFFBFC1C2).toIntArray(),
    "Sizzling Red" to Color(0xFFFF3855).toIntArray(),
    "Sizzling Sunrise" to Color(0xFFFFDB00).toIntArray(),
    "Skobeloff" to Color(0xFF007474).toIntArray(),
    "Sinopia" to Color(0xFFCB410B).toIntArray(),
    "Sky Blue" to Color(0xFF87CEEB).toIntArray(),
    "Sky Blue (Crayola)" to Color(0xFF76D7C4).toIntArray(),
    "Slate Blue" to Color(0xFF6A5ACD).toIntArray(),
    "Slate Gray" to Color(0xFF708090).toIntArray(),
    "Slimy Green" to Color(0xFF299617).toIntArray(),
    "Smitten" to Color(0xFFC84186).toIntArray(),
    "Smalt (Dark Blue)" to Color(0xFF003399).toIntArray(),
    "Smoky Black" to Color(0xFF100C08).toIntArray(),
    "Snow" to Color(0xFFFFFAFA).toIntArray(),
    "Solid Pink" to Color(0xFFFF69B4).toIntArray(),
    "Sonic Silver" to Color(0xFF757575).toIntArray(),
    "Space Cadet" to Color(0xFF1D2951).toIntArray(),
    "Spanish Bistre" to Color(0xFF807532).toIntArray(),
    "Spanish Blue" to Color(0xFF0070B8).toIntArray(),
    "Spanish Carmine" to Color(0xFFD10047).toIntArray(),
    "Spanish Gray" to Color(0xFF989898).toIntArray(),
    "Spanish Green" to Color(0xFF009150).toIntArray(),
    "Spanish Orange" to Color(0xFFE86100).toIntArray(),
    "Spanish Pink" to Color(0xFFF7BFBE).toIntArray(),
    "Spanish Red" to Color(0xFFE60026).toIntArray(),
    "Spanish Sky Blue" to Color(0xFF00FFFE).toIntArray(),
    "Spanish Violet" to Color(0xFF4C2882).toIntArray(),
    "Spanish Viridian" to Color(0xFF007F5C).toIntArray(),
    "Spring Bud" to Color(0xFFA7FC00).toIntArray(),
    "Spring Frost" to Color(0xFF87FF2A).toIntArray(),
    "Spring Green" to Color(0xFF00FF7F).toIntArray(),
    "Spring Green (Crayola)" to Color(0xFFECEBBD).toIntArray(),
    "Star Command Blue" to Color(0xFF007BB8).toIntArray(),
    "Steel Blue" to Color(0xFF4682B4).toIntArray(),
    "Steel Pink" to Color(0xFFCC33CC).toIntArray(),
    "Stil De Grain Yellow" to Color(0xFFFADA5E).toIntArray(),
    "Stizza" to Color(0xFF900910).toIntArray(),
    "Straw" to Color(0xFFE4D96F).toIntArray(),
    "Strawberry" to Color(0xFFFA5053).toIntArray(),
    "Strawberry Blonde" to Color(0xFFFF9361).toIntArray(),
    "Strong Lime Green" to Color(0xFF33CC33).toIntArray(),
    "Sugar Plum" to Color(0xFF914E75).toIntArray(),
    "Sunray" to Color(0xFFE3AB57).toIntArray(),
    "Sunset" to Color(0xFFFAD6A5).toIntArray(),
    "Super Pink" to Color(0xFFCF6BA9).toIntArray(),
    "Sweet Brown" to Color(0xFFA83731).toIntArray(),
    "Syracuse Orange" to Color(0xFFD44500).toIntArray(),
    "Sunglow" to Color(0xFFFFCC33).toIntArray(),
    "Sunset Orange" to Color(0xFFFF4F00).toIntArray(),
    "Tan" to Color(0xFFD2B48C).toIntArray(),
    "Tan (Crayola)" to Color(0xFFD99A6C).toIntArray(),
    "Tangerine" to Color(0xFFF28500).toIntArray(),
    "Tango Pink" to Color(0xFFE4717A).toIntArray(),
    "Tart Orange" to Color(0xFFFB4D46).toIntArray(),
    "Taupe" to Color(0xFF483C32).toIntArray(),
    "Taupe Gray" to Color(0xFF8B8589).toIntArray(),
    "Tea Green" to Color(0xFFD0F0C0).toIntArray(),
    "Tea Rose" to Color(0xFFF4C2C2).toIntArray(),
    "Teal" to Color(0xFF008080).toIntArray(),
    "Teal Blue" to Color(0xFF367588).toIntArray(),
    "Technobotanica" to Color(0xFF00FFBF).toIntArray(),
    "Telemagenta" to Color(0xFFCF3476).toIntArray(),
    "Tenné (Tawny)" to Color(0xFFCD5700).toIntArray(),
    "Terra Cotta" to Color(0xFFE2725B).toIntArray(),
    "Thistle" to Color(0xFFD8BFD8).toIntArray(),
    "Thulian Pink" to Color(0xFFDE6FA1).toIntArray(),
    "Tickle Me Pink" to Color(0xFFFC89AC).toIntArray(),
    "Tiffany Blue" to Color(0xFF0ABAB5).toIntArray(),
    "Timberwolf" to Color(0xFFDBD7D2).toIntArray(),
    "Titanium Yellow" to Color(0xFFEEE600).toIntArray(),
    "Tomato" to Color(0xFFFF6347).toIntArray(),
    "Tourmaline" to Color(0xFF86A1A9).toIntArray(),
    "Tropical Rainforest" to Color(0xFF00755E).toIntArray(),
    "True Blue" to Color(0xFF2D68C4).toIntArray(),
    "Trypan Blue" to Color(0xFF1C05B3).toIntArray(),
    "Tufts Blue" to Color(0xFF3E8EDE).toIntArray(),
    "Tumbleweed" to Color(0xFFDEAA88).toIntArray(),
    "Turquoise" to Color(0xFF40E0D0).toIntArray(),
    "Turquoise Blue" to Color(0xFF00FFEF).toIntArray(),
    "Turquoise Green" to Color(0xFFA0D6B4).toIntArray(),
    "Turtle Green" to Color(0xFF8A9A5B).toIntArray(),
    "Tuscan" to Color(0xFFFAD6A5).toIntArray(),
    "Tuscan Brown" to Color(0xFF6F4E37).toIntArray(),
    "Tuscan Red" to Color(0xFF7C4848).toIntArray(),
    "Tuscan Tan" to Color(0xFFA67B5B).toIntArray(),
    "Tuscany" to Color(0xFFC09999).toIntArray(),
    "Twilight Lavender" to Color(0xFF8A496B).toIntArray(),
    "Tyrian Purple" to Color(0xFF66023C).toIntArray(),
    "UA Blue" to Color(0xFF0033AA).toIntArray(),
    "UA Red" to Color(0xFFD9004C).toIntArray(),
    "Ultramarine" to Color(0xFF3F00FF).toIntArray(),
    "Ultramarine Blue" to Color(0xFF4166F5).toIntArray(),
    "Ultra Pink" to Color(0xFFFF6FFF).toIntArray(),
    "Ultra Red" to Color(0xFFFC6C85).toIntArray(),
    "Umber" to Color(0xFF635147).toIntArray(),
    "Unbleached Silk" to Color(0xFFFFDDCA).toIntArray(),
    "United Nations Blue" to Color(0xFF009EDB).toIntArray(),
    "University of Pennsylvania Red" to Color(0xFFA50021).toIntArray(),
    "Unmellow Yellow" to Color(0xFFFFFF66).toIntArray(),
    "UP Forest Green" to Color(0xFF014421).toIntArray(),
    "UP Maroon" to Color(0xFF7B1113).toIntArray(),
    "Upsdell Red" to Color(0xFFAE2029).toIntArray(),
    "Uranian Blue" to Color(0xFFAFDBF5).toIntArray(),
    "USAFA Blue" to Color(0xFF004F98).toIntArray(),
    "Van Dyke Brown" to Color(0xFF664228).toIntArray(),
    "Vanilla" to Color(0xFFF3E5AB).toIntArray(),
    "Vanilla Ice" to Color(0xFFF38FA9).toIntArray(),
    "Vegas Gold" to Color(0xFFC5B358).toIntArray(),
    "Venetian Red" to Color(0xFFC80815).toIntArray(),
    "Verdigris" to Color(0xFF43B3AE).toIntArray(),
    "Vermilion" to Color(0xFFE34234).toIntArray(),
    "Vermilion" to Color(0xFFD9381E).toIntArray(),
    "Veronica" to Color(0xFFA020F0).toIntArray(),
    "Violet" to Color(0xFF8F00FF).toIntArray(),
    "Violet (Color Wheel)" to Color(0xFF7F00FF).toIntArray(),
    "Violet (Crayola)" to Color(0xFF963D7F).toIntArray(),
    "Violet (RYB)" to Color(0xFF8601AF).toIntArray(),
    "Violet (Web)" to Color(0xFFEE82EE).toIntArray(),
    "Violet-Blue" to Color(0xFF324AB2).toIntArray(),
    "Violet-Blue (Crayola)" to Color(0xFF766EC8).toIntArray(),
    "Violet-Red" to Color(0xFFF75394).toIntArray(),
    "Violet-Red (PerBang)" to Color(0xFFF0599C).toIntArray(),
    "Viridian" to Color(0xFF40826D).toIntArray(),
    "Viridian Green" to Color(0xFF009698).toIntArray(),
    "Vivid Burgundy" to Color(0xFF9F1D35).toIntArray(),
    "Vivid Sky Blue" to Color(0xFF00CCFF).toIntArray(),
    "Vivid Tangerine" to Color(0xFFFFA089).toIntArray(),
    "Vivid Violet" to Color(0xFF9F00FF).toIntArray(),
    "Volt" to Color(0xFFCEFF00).toIntArray(),
    "Warm Black" to Color(0xFF004242).toIntArray(),
    "Weezy Blue" to Color(0xFF189BCC).toIntArray(),
    "Weezy Red" to Color(0xFFEA213A).toIntArray(),
    "Wheat" to Color(0xFFF5DEB3).toIntArray(),
    "White" to Color(0xFFFFFFFF).toIntArray(),
    "Wild Blue Yonder" to Color(0xFFA2ADD0).toIntArray(),
    "Wild Orchid" to Color(0xFFD470A2).toIntArray(),
    "Wild Strawberry" to Color(0xFFFF43A4).toIntArray(),
    "Wild Watermelon" to Color(0xFFFC6C85).toIntArray(),
    "Windsor Tan" to Color(0xFFA75502).toIntArray(),
    "Wine" to Color(0xFF722F37).toIntArray(),
    "Wine Dregs" to Color(0xFF673147).toIntArray(),
    "Winter Sky" to Color(0xFFFF007C).toIntArray(),
    "Wintergreen Dream" to Color(0xFF56887D).toIntArray(),
    "Wisteria" to Color(0xFFC9A0DC).toIntArray(),
    "Wood Brown" to Color(0xFFC19A6B).toIntArray(),
    "Xanadu" to Color(0xFF738678).toIntArray(),
    "Xanthic" to Color(0xFFEEED09).toIntArray(),
    "Xanthous" to Color(0xFFF1B42F).toIntArray(),
    "Yale Blue" to Color(0xFF00356B).toIntArray(),
    "Yellow" to Color(0xFFFFF000).toIntArray(),
    "Yellow (Crayola)" to Color(0xFFFCE883).toIntArray(),
    "Yellow (Munsell)" to Color(0xFFEFCC00).toIntArray(),
    "Yellow (NCS)" to Color(0xFFFFD300).toIntArray(),
    "Yellow (Pantone)" to Color(0xFFFEDF00).toIntArray(),
    "Yellow (Process)" to Color(0xFFFFEF00).toIntArray(),
    "Yellow (RYB)" to Color(0xFFFEFE33).toIntArray(),
    "Yellow-Green" to Color(0xFF9ACD32).toIntArray(),
    "Yellow-Green (Crayola)" to Color(0xFFC5E384).toIntArray(),
    "Yellow-Green (Color Wheel)" to Color(0xFF30B21A).toIntArray(),
    "Yellow Orange" to Color(0xFFFFAE42).toIntArray(),
    "Yellow Orange (Color Wheel)" to Color(0xFFFF9505).toIntArray(),
    "Yellow Sunshine" to Color(0xFFFFF700).toIntArray(),
    "YInMn Blue" to Color(0xFF2E5090).toIntArray(),
    "Zaffre" to Color(0xFF0014A8).toIntArray(),
    "Zinnwaldite Brown" to Color(0xFF2C1608).toIntArray(),
    "Zomp" to Color(0xFF39A78E).toIntArray()
)

val colorToIntArray = mapOf(
    "Absolute Zero" to intArrayOf(0, 72, 186),"Acid green" to intArrayOf(176, 191, 26),
    "Aero" to intArrayOf(124, 185, 232),
    "African violet" to intArrayOf(178, 132, 190),
    "Air superiority blue" to intArrayOf(114, 160, 193),
    "Alice blue" to intArrayOf(240, 248, 255),
    "Alizarin" to intArrayOf(219, 45, 67),
    "Alloy orange" to intArrayOf(196, 98, 16),
    "Almond" to intArrayOf(238, 217, 196),
    "Amaranthdeep purple" to intArrayOf(159, 43, 104),
    "Amaranth pink" to intArrayOf(241, 156, 187),
    "Amaranth purple" to intArrayOf(171, 39, 79),
    "Amazon" to intArrayOf(59, 122, 87),
    "Amber" to intArrayOf(255, 191, 0),
    "Amethyst" to intArrayOf(153, 102, 204),
    "Android green" to intArrayOf(61, 220, 132),
    "Antique brass" to intArrayOf(200, 138, 101),
    "Antique bronze" to intArrayOf(102, 93, 30),
    "Antique fuchsia" to intArrayOf(145, 92, 131),
    "Antique ruby" to intArrayOf(132, 27, 45),
    "Antique white" to intArrayOf(250, 235, 215),
    "Apricot" to intArrayOf(251, 206, 177),
    "Aqua" to intArrayOf(0, 255, 255),
    "Aquamarine" to intArrayOf(127, 255, 212),
    "Arctic lime" to intArrayOf(208, 255, 20),
    "Artichoke green" to intArrayOf(75, 111, 68),
    "Arylide yellow" to intArrayOf(233, 214, 107),
    "Ash gray" to intArrayOf(178, 190, 181),
    "Atomic tangerine" to intArrayOf(255, 153, 102),
    "Aureolin" to intArrayOf(253, 238, 0),
    "Azure" to intArrayOf(0, 127, 255),

)


//Function to calculate euclidean distance between two colors
fun calculateDistance(color1: IntArray, color2: IntArray): Double {
    return sqrt(
        (color1[0] - color2[0]).toDouble().pow(2) +
                (color1[1] - color2[1]).toDouble().pow(2) +
                (color1[2] - color2[2]).toDouble().pow(2)
    )
}

fun findClosestColor(rgb: IntArray): String {
    val maxDistance = sqrt(3 * 255.0.pow(2))
    var closestColor = ""
    var minDistance = Double.MAX_VALUE

    predefinedColors.forEach { (name, color) ->
        val distance = calculateDistance(color, rgb)
        if (distance < minDistance) {
            minDistance = distance
            closestColor = name
        }
    }
    //Convert distance to percentage
    val matchPercentage = ((maxDistance - minDistance) / maxDistance) * 100
    return "${matchPercentage.toBigDecimal().setScale(0, RoundingMode.UP)}% $closestColor"
}

// Extension function to convert Color to HEX string
fun Color.toHex(): String {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}

// Extension function to convert Color to RGB string
fun Color.toRgb(): String {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return "RGB($red, $green, $blue)"
}

fun Color.toIntArray(): IntArray {
    return intArrayOf(
        (red * 255).toInt(),(green * 255).toInt(),
        (blue * 255).toInt(),
        (alpha * 255).toInt()
    )
}

//Helper function to convert ImageProxy to Bitmap
fun imageProxyToBitmap(image: ImageProxy) : Bitmap? {
    val buffer: ByteBuffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap: Bitmap? = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap?.let { rotateBitmap(it, image.imageInfo.rotationDegrees) }
}

// Function to pick a color from the bitmap at the given position
fun pickColorFromBitmap(bitmap: Bitmap, position: Offset): Color {
    val x = position.x.toInt().coerceIn(0, bitmap.width - 1)
    val y = position.y.toInt().coerceIn(0, bitmap.height - 1)
    val pixel = bitmap.getPixel(x, y)
    return Color(pixel)
}

// Helper function to rotate a Bitmap
fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height, matrix, true)
}

fun transformCoordinates(pickerPosition: Offset, imageBitmap: Bitmap, size: IntSize): Offset {
    val imageWidth = imageBitmap.width.toFloat()
    val imageHeight = imageBitmap.height.toFloat()

    // Calculate the aspect ratios
    val viewAspectRatio = size.width.toFloat() / size.height.toFloat()
    val imageAspectRatio = imageWidth / imageHeight

    // Determine scaling and offset
    val scale: Float
    val offsetX: Float
    val offsetY: Float
    if (viewAspectRatio > imageAspectRatio) {
        scale = size.height.toFloat() / imageHeight
        offsetX = (size.width.toFloat() - imageWidth * scale) / 2
        offsetY = 0f
    } else {
        scale = size.width.toFloat() / imageWidth
        offsetX = 0f
        offsetY = (size.height.toFloat() - imageHeight * scale) / 2
    }

    // Transform picker position to bitmap coordinates
    val transformedX = (pickerPosition.x - offsetX) / scale
    val transformedY = (pickerPosition.y - offsetY) / scale

    return Offset(transformedX, transformedY)
}

fun transformCoordinates(pickerPosition: Offset, imageProxy: ImageProxy, size: IntSize): Offset {
    val imageWidth = imageProxy.width.toFloat()
    val imageHeight = imageProxy.height.toFloat()

    // Calculate the aspect ratios
    val viewAspectRatio = size.width.toFloat() / size.height.toFloat()
    val imageAspectRatio = imageWidth / imageHeight

    // Determine scaling and offset
    val scale: Float
    val offsetX: Float
    val offsetY: Float
    if (viewAspectRatio > imageAspectRatio) {
        scale = size.height.toFloat() / imageHeight
        offsetX = (size.width.toFloat() - imageWidth * scale) / 2
        offsetY = 0f
    } else {
        scale = size.width.toFloat() / imageWidth
        offsetX = 0f
        offsetY = (size.height.toFloat() - imageHeight * scale) / 2
    }

    // Transform picker position to bitmap coordinates
    val transformedX = (pickerPosition.x - offsetX) / scale
    val transformedY = (pickerPosition.y - offsetY) / scale

    return Offset(transformedX, transformedY)
}

//Function to smooth color detection. The lower the alpha the smoother the detection
fun updateEmaColor(newColor: IntArray, alpha: Double = 1.0, threshold: Int = 5): IntArray {
    val (newRed, newGreen, newBlue) = newColor

    if (!initialized) {
        // Initialize EMA with the first color
        emaRed = newRed.toDouble()
        emaGreen = newGreen.toDouble()
        emaBlue = newBlue.toDouble()
        initialized = true
    } else {
        // Calculate the difference
        val deltaRed = Math.abs(newRed - emaRed)
        val deltaGreen = Math.abs(newGreen - emaGreen)
        val deltaBlue = Math.abs(newBlue - emaBlue)

        // Update EMA only if the change is significant
        if (deltaRed > threshold || deltaGreen > threshold || deltaBlue > threshold) {
            emaRed = alpha * newRed + (1 - alpha) * emaRed
            emaGreen = alpha * newGreen + (1 - alpha) * emaGreen
            emaBlue = alpha * newBlue + (1 - alpha) * emaBlue
        }
    }

    // Return the smoothed color
    return intArrayOf(emaRed.toInt(), emaGreen.toInt(), emaBlue.toInt())
}

//Function to smooth color detection. The lower the alpha the smoother the detection
fun updateEmaColor(newColor: IntArray, alpha: Double = 0.2): IntArray {
    val (newRed, newGreen, newBlue) = newColor

    if (!initialized) {
        // Initialize EMA with the first color
        emaRed = newRed.toDouble()
        emaGreen = newGreen.toDouble()
        emaBlue = newBlue.toDouble()
        initialized = true
    } else {
        // Update EMA
        emaRed = alpha * newRed + (1 - alpha) * emaRed
        emaGreen = alpha * newGreen + (1 - alpha) * emaGreen
        emaBlue = alpha * newBlue + (1 - alpha) * emaBlue
    }

    // Return the smoothed color
    return intArrayOf(emaRed.toInt(), emaGreen.toInt(), emaBlue.toInt())
}

fun getAverageColorAtOffset(image: ImageProxy, offset: Offset, areaSize: Int = 3): IntArray {
    val planes = image.planes

    val height = image.height
    val width = image.width

    // Helper function to convert ByteBuffer to byte array
    fun byteBufferToByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }

    // Y
    val yArr = byteBufferToByteArray(planes[0].buffer)
    val yPixelStride = planes[0].pixelStride
    val yRowStride = planes[0].rowStride

    // U
    val uArr = byteBufferToByteArray(planes[1].buffer)
    val uPixelStride = planes[1].pixelStride
    val uRowStride = planes[1].rowStride

    // V
    val vArr = byteBufferToByteArray(planes[2].buffer)
    val vPixelStride = planes[2].pixelStride
    val vRowStride = planes[2].rowStride

    var rSum = 0.0
    var gSum = 0.0
    var bSum = 0.0
    var count = 0

    // Calculate the range to average around the target pixel
    for (dy in -areaSize / 2..areaSize / 2) {
        for (dx in -areaSize / 2..areaSize / 2) {
            // Ensure the offset is within the image bounds
            val x = (offset.x + dx).coerceIn(0f, (width - 1).toFloat()).toInt()
            val y = (offset.y + dy).coerceIn(0f, (height - 1).toFloat()).toInt()

            // Calculate indices for the specified offset
            val yIndex = y * yRowStride + x * yPixelStride
            val yValue = yArr[yIndex].toInt() and 0xFF

            // Compute the UV index for the specified offset
            val uvIndex = (y / 2) * uRowStride + (x / 2) * uPixelStride
            val uValue = (uArr[uvIndex].toInt() and 0xFF) - 128
            val vValue = (vArr[uvIndex].toInt() and 0xFF) - 128

            val r = yValue + 1.370705 * vValue
            val g = yValue - 0.698001 * vValue - 0.337633 * uValue
            val b = yValue + 1.732446 * uValue

            // Accumulate the RGB values
            rSum += r.coerceIn(0.0, 255.0)
            gSum += g.coerceIn(0.0, 255.0)
            bSum += b.coerceIn(0.0, 255.0)
            count++
        }
    }

    // Calculate the average RGB values
    val rAverage = (rSum / count).toInt()
    val gAverage = (gSum / count).toInt()
    val bAverage = (bSum / count).toInt()

    // Return the averaged RGB values as an IntArray
    return intArrayOf(rAverage, gAverage, bAverage)
}





private fun imageProxyToBitmapLive(image: ImageProxy): Bitmap? {
    val yBuffer = image.planes[0].buffer // Y
    val uBuffer = image.planes[1].buffer // U
    val vBuffer = image.planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val imageBytes = out.toByteArray()
    val bitmap: Bitmap? = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    return bitmap?.let { Bitmap.createBitmap(it) }
}


private fun imageProxyToBitmapLiveRGBA(image: ImageProxy): Bitmap? {
    val planes = image.planes
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * image.width

    val bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(buffer)

    val rotationDegrees = image.imageInfo.rotationDegrees
    Log.d("ColorPicker", "Image rotation degrees: $rotationDegrees")

    return rotateBitmap(bitmap, rotationDegrees)
}


fun getSmoothedPixelColorAtOffset(image: ImageProxy, offset: Offset, smoothingFactor: Double, sampleRadius: Int): IntArray {
    val sampledColors = mutableListOf<IntArray>()

    // Sample multiple points around the offset
    for (dx in -sampleRadius..sampleRadius) {
        for (dy in -sampleRadius..sampleRadius) {
            val sampledOffset = Offset(offset.x + dx, offset.y + dy)
            sampledColors.add(getPixelColorAtOffset(image, sampledOffset))
        }
    }

    // Calculate the average color of the sampled points
    val avgRed = sampledColors.map { it[0] }.average()
    val avgGreen = sampledColors.map { it[1] }.average()
    val avgBlue = sampledColors.map { it[2] }.average()

    if (smoothingFactor == 1.0) {
        // Directly use the average color when smoothingFactor is 1.0
        emaRed = avgRed
        emaGreen = avgGreen
        emaBlue = avgBlue
    } else {
        // Apply EMA for smoothing
        emaRed = (smoothingFactor * avgRed) + ((1 - smoothingFactor) * emaRed)
        emaGreen = (smoothingFactor * avgGreen) + ((1 - smoothingFactor) * emaGreen)
        emaBlue = (smoothingFactor * avgBlue) + ((1 - smoothingFactor) * emaBlue)
    }

    // Apply EMA for smoothing
    emaRed = (smoothingFactor * avgRed) + ((1 - smoothingFactor) * emaRed)
    emaGreen = (smoothingFactor * avgGreen) + ((1 - smoothingFactor) * emaGreen)
    emaBlue = (smoothingFactor * avgBlue) + ((1 - smoothingFactor) * emaBlue)

    return intArrayOf(emaRed.toInt(), emaGreen.toInt(), emaBlue.toInt())
}


fun getPixelColorAtOffset(image: ImageProxy, offset: Offset): IntArray {
    val planes = image.planes

    val height = image.height
    val width = image.width

    // Ensure the offset is within the image bounds
    val x = offset.x.coerceIn(0f, (width - 1).toFloat()).toInt()
    val y = offset.y.coerceIn(0f, (height - 1).toFloat()).toInt()

    // Helper function to convert ByteBuffer to byte array
    fun byteBufferToByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }

    // Y
    val yArr = byteBufferToByteArray(planes[0].buffer)
    val yPixelStride = planes[0].pixelStride
    val yRowStride = planes[0].rowStride

    // U
    val uArr = byteBufferToByteArray(planes[1].buffer)
    val uPixelStride = planes[1].pixelStride
    val uRowStride = planes[1].rowStride

    // V
    val vArr = byteBufferToByteArray(planes[2].buffer)
    val vPixelStride = planes[2].pixelStride
    val vRowStride = planes[2].rowStride

    // Calculate indices for the specified offset
    val yIndex = y * yRowStride + x * yPixelStride
    val yValue = yArr[yIndex].toInt() and 0xFF

    // Compute the UV index for the specified offset
    val uvIndex = (y / 2) * uRowStride + (x / 2) * uPixelStride
    val uValue = (uArr[uvIndex].toInt() and 0xFF) - 128
    val vValue = (vArr[uvIndex].toInt() and 0xFF) - 128

    val r = yValue + 1.370705 * vValue
    val g = yValue - 0.698001 * vValue - 0.337633 * uValue
    val b = yValue + 1.732446 * uValue

    // Clamp the RGB values to [0, 255]
    val rClamped = r.coerceIn(0.0, 255.0).toInt()
    val gClamped = g.coerceIn(0.0, 255.0).toInt()
    val bClamped = b.coerceIn(0.0, 255.0).toInt()

    // Return the RGB values as an IntArray
    return intArrayOf(rClamped, gClamped, bClamped)
}

// Smooth color using a simple moving average
//SmoothColor function takes the new color value and the color queue, computes the average of the colors in the queue, and returns the smoothed color.
//If the queue reaches a predefined maximum size (maxQueueSize), the oldest color is removed before adding the new color.
private fun smoothColor(newColor: Color, colorQueue: ArrayDeque<Color>, maxQueueSize: Int = 20) : Color {
    if (colorQueue.size >= maxQueueSize) {
        colorQueue.removeFirst()
    }

    colorQueue.addLast(newColor)

    var r = 0f
    var g  = 0f
    var b = 0f

    for ((index,color) in colorQueue.withIndex()) {
        val weight = (index + 1) / colorQueue.size.toFloat()
        r += color.red * weight
        g += color.green * weight
        b += color.blue * weight
    }

    val size = colorQueue.size
    return Color(r / size, g / size, b / size)
}


// Function to convert YUV to RGB
private fun yuvToRgb(image: ImageProxy): Bitmap {
    val yBuffer = image.planes[0].buffer // Y
    val uBuffer = image.planes[1].buffer // U
    val vBuffer = image.planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Y channel
    yBuffer.get(nv21, 0, ySize)

    // VU channel
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

private fun updateEMAColor(imageProxy: ImageProxy, position: Offset, alpha: Float = 0.5f,selectedColor: Color = Color.Transparent) : Color {
    val yBuffer = imageProxy.planes[0].buffer // Y plane
    val uBuffer = imageProxy.planes[1].buffer // U plane
    val vBuffer = imageProxy.planes[2].buffer // V plane

    val width = imageProxy.width
    val height = imageProxy.height

    val x = position.x.toInt().coerceIn(0, width - 1)
    val y = position.y.toInt().coerceIn(0, height - 1)

    val yRowStride = imageProxy.planes[0].rowStride
    val uvRowStride = imageProxy.planes[1].rowStride
    val uvPixelStride = imageProxy.planes[1].pixelStride

    val yIndex = y * yRowStride + x
    val uvIndex = (y / 2) * uvRowStride + (x / 2) * uvPixelStride

    val yValue = yBuffer[yIndex].toInt() and 0xFF
    val uValue = uBuffer[uvIndex].toInt() and 0xFF
    val vValue = vBuffer[uvIndex].toInt() and 0xFF

    val rgb = yuvToRgb(yValue, uValue, vValue)

    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF

    val newColor = Color(r, g, b)

    // Apply EMA filter
    val color = Color(
        red = alpha * newColor.red + (1 - alpha) * selectedColor.red,
        green = alpha * newColor.green + (1 - alpha) * selectedColor.green,
        blue = alpha * newColor.blue + (1 - alpha) * selectedColor.blue
    )
    return color
}

private fun yuvToRgb(y: Int, u: Int, v: Int): Int {
    val c = y - 16
    val d = u - 128
    val e = v - 128

    val r = (298 * c + 409 * e + 128) shr 8
    val g = (298 * c - 100 * d - 208 * e + 128) shr 8
    val b = (298 * c + 516 * d + 128) shr 8

    return (0xFF shl 24) or (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)
}
