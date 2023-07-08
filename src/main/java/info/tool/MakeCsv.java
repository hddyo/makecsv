package info.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CSV作成ツール bug3
 */
public class MakeCsv {

  /** ルートエレメント */
  private final Element root;

  /** コンストラクタ */
  private MakeCsv(String xmlPath, String logPath) throws Exception {

    try {
      File xmlFile = new File(xmlPath);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(xmlFile);
      this.root = document.getDocumentElement();

    } catch (Exception e) {
      throw e;
    }

  }

  /**
   * メイン
   * @param args　xmlパス
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    MakeCsv clazz = new MakeCsv(args[0], null);
    clazz.exexute();

  }

  public void exexute() throws Exception {
    System.out.println("Hello World!!");

    NodeList setNodeList = this.root.getElementsByTagName("set");
    Element setTag = (Element) setNodeList.item(0);

    try {
      Element outFileTag = (Element) setTag.getElementsByTagName("outFile").item(0);
      String path = outFileTag.getAttribute("path");
      System.out.println(path);
      this.parse(outFileTag.getAttribute("path"),
          outFileTag.getAttribute("encode"),
          outFileTag.getAttribute("lncode"),
          outFileTag.getAttribute("type"),
          Integer.parseInt(outFileTag.getAttribute("line")),
          Boolean.valueOf(outFileTag.getAttribute("csv_header")),
          outFileTag.getElementsByTagName("column"));

    } catch (Exception e) {
      throw e;
    }

  }

  public void parse(
      String outPath,
      String outEnc,
      String outLnCode,
      String outType,
      Integer outLine,
      Boolean outCsvHeader,
      NodeList columnNodes) throws Exception {

    String dataStr = "";
    File outFile = new File(outPath);

    try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile), outEnc)) {

      // XMLパース
      List<ColumnBean> columnList;
      columnList = IntStream.range(0, columnNodes.getLength()).mapToObj(
          columnNodes::item).map(n -> {
            Element e = (Element) n;
            ColumnBean col = new ColumnBean();
            col.setName(e.getAttribute("name"));
            col.setByte_length(Integer.valueOf(e.getAttribute("byte_length")));
            col.setFull(Boolean.valueOf(e.getAttribute("full")));
            col.setRandom(Boolean.valueOf(e.getAttribute("random")));
            col.setType(e.getAttribute("type"));
            col.setIdx(Boolean.valueOf(e.getAttribute("idx")));
            if (e.getAttribute("idx_start") != "") {
              col.setIdx_start(Integer.valueOf(e.getAttribute("idx_start")));
            } else {
              col.setIdx_start(1);
            }
            col.setIdx_space(Boolean.valueOf(e.getAttribute("idx_space")));
            col.setLsetlen(e.getAttribute("lsetlen"));
            col.setRsetlen(e.getAttribute("rsetlen"));
            return col;
          }).collect(Collectors.toList());

      // ヘッダ行出力
      for (Integer j = 0; j < columnNodes.getLength(); j++) {
        if (j == 0) {
          dataStr = dataStr + "\"";
        } else {
          dataStr = dataStr + ",\"";
        }
        String name = columnList.get(j).getName();
        dataStr = dataStr + name + "\"";
      }

      // 改行コード
      switch (outLnCode) {
        case "CRLF":
          osw.write(dataStr + "\r\n");
          break;
        case "CR":
          osw.write(dataStr + "\r");
          break;
        case "LF":
          osw.write(dataStr + "\n");
          break;
      }

      // 明細出力
      for (Integer i = 0; i < outLine; i++) {

        // 初期化
        dataStr = "";

        // 定義出力
        for (Integer j = 0; j < columnNodes.getLength(); j++) {

          if (j == 0) {
            dataStr = dataStr + "\"";
          } else {
            dataStr = dataStr + ",\"";
          }

          // 設定値取得
          String name = columnList.get(j).getName();
          Integer byte_length = columnList.get(j).getByte_length();
          Boolean full = columnList.get(j).isFull();
          Boolean random = columnList.get(j).isRandom();
          String type = columnList.get(j).getType();
          Boolean idx = columnList.get(j).isIdx();
          Integer idx_start = columnList.get(j).getIdx_start();
          Boolean idx_space = columnList.get(j).isIdx_space();
          String lsetlen = columnList.get(j).getLsetlen();
          String rsetlen = columnList.get(j).getRsetlen();

          if (idx) {
            // （インデックス）

            // 連番値の桁数が設定値を超える場合はエラー
            if (byte_length < String.valueOf(i + 1 + (idx_start - 1)).length()) {
              throw new Exception(name + "：連番値の桁数が設定バイト数を超えています。");
            }

            if (idx_space) {
              Integer spaceCnt = byte_length - String.valueOf(i + 1 + (idx_start - 1)).length();

              for (Integer forspace = 0; forspace < spaceCnt; forspace++) {
                dataStr = dataStr + " ";
              }
              dataStr = dataStr + String.valueOf(i + 1 + (idx_start - 1));

            } else {

              String idxFormat = "%0" + String.valueOf(byte_length) + "d";
              dataStr = dataStr + String.format(idxFormat, i + 1 + (idx_start - 1));

            }

          } else {
            Integer lackByteLen = 0;
            if (full) {
              int llenbyte = 0;
              char[] llen = lsetlen.toCharArray();
              for (int llencnt = 0; llencnt < llen.length; llencnt++) {
                if (String.valueOf(llen[llencnt]).getBytes().length <= 1) {
                  llenbyte += 1;
                } else {
                  llenbyte += 2;
                }
              }
              int rlenbyte = 0;
              char[] rlen = rsetlen.toCharArray();
              for (int rlencnt = 0; rlencnt < rlen.length; rlencnt++) {
                if (String.valueOf(rlen[rlencnt]).getBytes().length <= 1) {
                  rlenbyte += 1;
                } else {
                  rlenbyte += 1;
                }
              }
              lackByteLen = byte_length - llenbyte - rlenbyte;
            } else {
              lackByteLen = byte_length - lsetlen.length() - rsetlen.length();
            }

            // 左文字固定文字列セット
            if (lsetlen.length() != 0) {
              dataStr = dataStr + lsetlen;
            }

            // 乱数値
            int intran;

            // 不足文字数文字埋め
            for (Integer lacklensetcnt = 0; lacklensetcnt < lackByteLen;) {

              if (random) {
                switch (type) {
                  case "数値":
                    if (full && (lackByteLen - lacklensetcnt) != 1) {
                      // 全角数字かつ残バイト数が１でない場合
                      String[] full_suuji_list = { "０", "１", "２", "３", "４", "５", "６", "７", "８",
                          "９" };
                      intran = (int) (Math.random() * 10);
                      dataStr = dataStr + full_suuji_list[intran % 10];
                    } else {
                      // 半角数値 又は、残バイト数が１の場合
                      intran = (int) (Math.random() * 10);
                      dataStr = dataStr + String.valueOf(intran);
                    }
                    break;

                  case "英字":
                    if (full && (lackByteLen - lacklensetcnt) != 1) {
                      String[] full_eiji_list = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
                          "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                          "W", "X", "Y", "Z" };
                      intran = (int) (Math.random() * 26);
                      dataStr = dataStr + full_eiji_list[intran % 26];
                    } else {
                      String[] half_eiji_list = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
                          "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                          "W", "X", "Y", "Z" };
                      intran = (int) (Math.random() * 26);
                      dataStr = dataStr + half_eiji_list[intran % 26];
                    }
                    break;

                  case "漢字":
                    if (full && (lackByteLen - lacklensetcnt) != 1) {
                      // 全角数字かつ残バイト数が１でない場合
                      String[] full_kanji_list = { "零", "一", "二", "三", "四", "五", "六", "七", "八",
                          "九", "十" };
                      intran = (int) (Math.random() * 10);
                      dataStr = dataStr + full_kanji_list[intran % 10];
                    } else {
                      // 残バイト数が１の場合
                      dataStr = dataStr + " ";
                    }
                    break;

                  case "ひらがな":
                    if (full && (lackByteLen - lacklensetcnt) != 1) {
                      // 全角数字かつ残バイト数が１でない場合
                      String[] full_hira_list = { "あ", "い", "う", "え", "お", "か", "き", "く", "け",
                          "こ", "さ", "し", "す", "せ", "そ", "た", "ち", "つ", "て", "と", "な", "に",
                          "ぬ", "ね", "の", "は", "ひ", "ふ", "へ", "ほ", "ま", "み", "む", "め", "も",
                          "ら", "り", "る", "れ", "ろ", "や", "ゆ", "よ", "わ", "を", "ん" };
                      intran = (int) (Math.random() * 10);
                      dataStr = dataStr + full_hira_list[intran % 10];
                    } else {
                      // 残バイト数が１の場合
                      dataStr = dataStr + " ";
                    }
                    break;

                  case "カタカナ":
                    if (full && (lackByteLen - lacklensetcnt) != 1) {
                      // 全角カナかつ残バイト数が１でない場合
                      String[] full_kana_list = { "ア", "イ", "ウ", "エ", "オ", "カ", "キ", "ク", "ケ",
                          "コ",
                          "サ", "シ", "ス", "セ", "ソ", "タ", "チ", "ツ", "テ", "ト", "ナ", "ニ",
                          "ヌ", "ネ", "ノ", "ハ", "ヒ", "フ", "ヘ", "ホ", "マ", "ミ", "ム", "メ",
                          "モ", "ヤ", "ユ", "ヨ", "ラ", "リ", "ル", "レ",
                          "ロ", "ワ", "ヲ", "ン" };
                      intran = (int) (Math.random() * 46);
                      dataStr = dataStr + full_kana_list[intran % 46];
                    } else {
                      // 半角カナもしくは残バイト数が１の場合
                      String[] half_kana_list = { "ｱ", "ｲ", "ｳ", "ｴ", "ｵ", "ｶ", "ｷ", "ｸ", "ｹ",
                          "ｺ", "ｻ", "ｼ", "ｽ", "ｾ", "ｿ", "ﾀ", "ﾁ", "ﾂ", "ﾃ", "ﾄ", "ﾅ", "ﾆ",
                          "ﾇ", "ﾈ", "ﾉ", "ﾊ", "ﾋ", "ﾌ", "ﾍ", "ﾎﾞ", "ﾏ", "ﾐ", "ﾑ", "ﾒ",
                          "ﾓ", "ﾔ", "ﾕ", "ﾖ", "ﾗ", "ﾘ", "ﾙ", "ﾚ",
                          "ﾛ", "ﾜ", "ｵ", "ﾝ" };
                      intran = (int) (Math.random() * 46);
                      dataStr = dataStr + half_kana_list[intran % 46];
                    }
                    break;
                }

              } else {
                // random 未設定時
                // スペース埋め
                if (full && (lackByteLen - lacklensetcnt) != 1) {
                  // 全角スペース
                  dataStr = dataStr + "　";
                } else {
                  // 半角スペース
                  dataStr = dataStr + " ";
                }
              }

              // 残り文字数カウント
              if (full) {
                lacklensetcnt += 2;
              } else {
                lacklensetcnt++;
              }
            }

            // 右文字固定文字列セット
            if (rsetlen.length() != 0) {
              dataStr = dataStr + rsetlen;
            }

          }

          // 項目囲み終端（ダブルクォーテーション）
          dataStr = dataStr + "\"";

        }

        // 改行出力
        switch (outLnCode) {
          case "CRLF":
            osw.write(dataStr + "\r\n");
            break;
          case "CR":
            osw.write(dataStr + "\r");
            break;
          case "LF":
            osw.write(dataStr + "\n");
            break;
        }

      }

      System.out.println("とおったよ");

    } catch (Exception e) {
      throw e;
    }
  }
}