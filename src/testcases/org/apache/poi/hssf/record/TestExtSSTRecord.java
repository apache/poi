/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hssf.record;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.poifs.storage.RawDataUtil;
import org.junit.jupiter.api.Test;

final class TestExtSSTRecord {

    /**
     *  ExtSSTRecord can be continued. Ensure we properly read the continue remainder.
     */
    @Test
    void test50967() throws IOException {
        // hex dump from ISBN_UPD_PL_20100104_1525.xls attached to Bugzilla 50867
        byte[] bytes = RawDataUtil.decompress("H4sIAAAAAAAAACWaBbAVxxZF9/jgrkFDcAvu7hrc3d3dLbhrgBDcXR8OwV2CEz" +
          "RIIFhw+4G/Zqiiqrn3zkyvvc/p7tM976sSp/CVOJIUVVJH2sa022lX0e6OLK0zpE9RpKemdCQav9lSlhhSCldqEEuq7EsbYkuLuGdJHG" +
          "km90yNJ43ggZETSP/jnuwJpR+4Z0QiqVdMaU5iaTz3NkoiVeGeH5JK8eNKtZJJZbk3SXIpenzpOu0p2l4ppTY8I0sq+uXeBT/Qx3dwpY" +
          "aLZ+xMK63h3tjpJfBUOYNUFO4FGbkO7mOZuMaSmmWRajhSnaxSefjn/AiHJ5nZpTfoyJ1DSgf/jzml79H8P9rntF9ySf+iJ00eKWF0fM" +
          "iLD+j5lI/+0TOxgDQEDzIVkpKho2BhWNExpIjUDf4eRaVWeDG2mDQAHS2KoxUvZpaQRqNnWEmuQU+vUuhEz5DS3Icno8tI/dA1pCyf8a" +
          "JVOQkk9SovlUTfuQrSb+h7VVHaj77rlaQVGLDlJ56JvthVpTvoy1BN+oC+gtWJCfo61eD/6NpdEyZ0xa0t/UWcCtbhd3TlrouP6CpfD7" +
          "3EqXx9WvStaQAP8TrQUJqMzkwkyyd09mgiFUdfrWbEEl2xW9Avuh61lLah50Qr6Rf0xG1DP+ip1fZbnJ62IwbouNEeDnQ87yD9io5Wnb" +
          "iWOC3rTLzQMbUL8UTHw67SdHSU7S69I9/W9JDqoWNRT/IIHYd64RM6KvchdsQpbj9Y0RG1P/EPcnUA36Oj3kDJh//QIK6Hf+cQfIc/yT" +
          "BYiU+j4dwD/84RfE9cHv5Mv+goPYo8Qcf00eglHj+MlWLCP2Sc1AH+iePxB/5hE/AYgTUmwo2OJpOkiuhoM5nv0DF1ijQIHcemEit0ZJ" +
          "qOv+iYOIP70bFqJrmBjk+z6BsdhWcTQ3TUmMPz0HFxLvFGx4FfGZ/EI9lvxA0db2jv0N6Z/23c+Av5Dl3RF/EsdI1dTO6gq89SchA9lZ" +
          "dJ+dFTfDn5Slz8lVyPrr0M/gnoyrlGsoJxs1bKhp4266Q86Km2XkqJnlsbpLXoWbFR6o2ebZukn9FzfbO0kjx7ukXaha5/tko7gvkgQr" +
          "qGrsjbadGVZof0lnzrtlMqhq6ru6Tl6Mq/R4pEfDrslQqgZ/Q+qTZ6ru7nd/Q0OSBlJc9GHJSqo2fFIfonTmcOS/PQZR6VzqOrzjEpLX" +
          "pqHf+Wb+dO4Du6Lp4kRsQr72n8Rd/9s8QEPb3OE1N0xLyAB+iocpGxj47pl/AoGCdX8Bz+qVeJA9xPr5G/xCXFDfqGe9BNYgl3g9tohH" +
          "f7nW/j/te7jCH8L3mPsQr3svvEEt4PD/g/vBf/hgf/szxmLMB77B+pJ5wFn0qP4Wz3TEqAgN3Ppbb4n/Bf6TC8515KA+Ft9VqKA+f4N1" +
          "K+YK5+K82GN8N76Qq8Iz4QP3gXfJQqwet+ljbj90PaaXD7/0lbyaNWX3gOfl//Ko2Cv5ph6Cv8PUxDKYP5yjJUAr9324bawt/NNZQcny" +
          "96hoYG4zqSEfr8v8iGVsHfKKqhqPi7LZqhFvzwJrqhRfD3YNB8T94otqF18JePY+gDfheNa+g5OlrFMxQX/u3xDbWCv3BCQ0/JlzqJDL" +
          "nwd0tsKEUwLyXhPvgnJjVUEN8fJTM0E/4VKQzVgnt0KkO54VVq+oF3XRpDDYJ5NR3f4/PM9IZYQvQwgxH6vSiToWzwFs1i6Cy827MaKo" +
          "PPLbIZeg3v/uyGfoIzSU5Dq+FckstQjsDnPIZ2wDknr6H0+Dw9v6FU8I0sYCgefCULGbqAv50KG/oPzvEsTt/BV7I435MX0ZnM58Hplj" +
          "I0A86dpQ2Vg/OHcoY2w7movBFYpUwVDf1KPlysZKgkfI2qGDoD37Cqhl7B92s1/IbvtxqGopMHHWoZugFf8dqGNsHXpY6hO/jYqZ6hW/" +
          "g4soGhd/C9amioGVwLGhuKSdw7NOU++Dq0oIXr31aGmsBTua2hvfC0aWeIFFWnDob+hWd7R0P54anW2dBF4nuki6Gy8AzpZsiE50Z3Q4" +
          "3w6xUm94CnQ2/iDE/pvoaO4depfoYq49eQAVyPTz6DdCw8QwbzGZ7SQ7kOnjTDDa0gD5eMMJQGn8yRhkYwft6MMtSL/Gsz5lv+VR4HH3" +
          "7ZTGILiOeXiYbmwLVosqHCcC2bYqg4eddrGr7C1W0GfsGVe5ahP+CqMpt8g+uvuYYG4U+7eeQdPOnmGzpEfv270NB44rZ9saEa+BN3ma" +
          "ENcAxYbighHH+t4D58Gs8gILU0fg0tHMsopkrC0WsD+Q3Hgk3w4E/sLdwPR5+thuLDsWQbfHA0iDD0idbfbmgZcTu3w1An4vbHLkNd4O" +
          "qwx1BkuPrs4z58WrffUEV82vm7oTrwmQfRj187aevA+e6Qoen41uUoeuF8dMzQSDjXnDA0Gs7RpxincE4+Y6gDnA3OGcoLZ6s/4IHz3Q" +
          "X8gbPyJfTAuegKOuHaexV/8WvddeJGHAfdMFSPOL65aegcfL/cJu7wrbpLf0EteI/O4Blxn7yCp85DQznJr+uPDG2DZxnBIxVV8injJc" +
          "ivZ4a6EtiELwydhyv/y2/jsfBrQ+/h+vWNoZrke4/3hjLDk/ujoRfwrPpsqDk+NfnPUCI4mnylhWOTYao9HCMsU4XgKO6a+kz8bvmmZt" +
          "F/8SimYtHvgmimOtNvupim3jPOvvDlafKmRRxTOfChYlxT39Hv1XimluPHkfimZhCvDwlMHYXjUCJT04iX+Z2p8/iyJImp7vjSL6mpcv" +
          "BsSmZqKPnUKoWpXMRtU0o+w1UtlamUcG35wdRw/Hma2tQu+HKnNeUQt0bpTGWGc0V6U5RcKp/RlA3viUwmmliPspi6DveRrKaGwJ0mmx" +
          "nWgfmzm2Gd2ymHGda5b3KaWgf/9VympsN/P7ep34J8y2vqAPzV8pmKCv/M/KaqwF+woBnOF38UMjUe/h+LmPoH/n5FTeUM1oliprLA/6" +
          "Y4zyXvWpQ0lQz+LaVMdYL/U2lTW4hz+bLwwl+xnBmO1zrlzbCO+qUC/aDjTkVTv6CjbGU+EO8sP5l6SLuXH3uhI2E1U+eCda86z0fHzh" +
          "qmuqHjVU1Tq9BRsDY60dGujqnvyYOLdeEN1un6pq6io2ID+iUO/6PdEsSjkans6NjS2FQH8uNTE1Ob0DOomanc6CjYnOfRnmlhaiR63r" +
          "VEH3oStoYDPcPamMqPngVtTbEV0Jl2ppgqdaq9KS7RZYrDruiJ3snUSgT/0NnUPvSU72LqAfHp19VUPPTU62bqLe2C7jwHXdt6mKoaxK" +
          "enqZ5BfHqZGoyuFH3IB+Kjvqbmou9D3295Fr+/qY3ocweYWkicPtHOQF+GQaYOB3XJYPIYnaWHmLqLzhpDTb1AZxeKoSjoazPclEXeFR" +
          "1h6k/a+D/zPPSmG2nqYDA+aPui1x9tajF5eIa2LbpfjTFFqa0jFPGV0X11nCks1Jbxpgqie9EEUxnQ/Xwi8UO3O9nUxGB/MsXUHOL5gX" +
          "YIulNMM7WC8Rx5hqmpwb6LdmQwrmby3KDummUqcTC+fzG1l9acbWos+tPNMbUB/fXmmvoL/T1YJIM6v8M84ob+4mySTqB/8nxTCdGdc6" +
          "Gp3ehOtsjUMvK1+GJ+R3+zJeQzunss5X50b1hmKi96660ww33LyJXf8vXH1Wa4L76+xlQd9B1aa6o4P4xfZyoy+g6t5zP6Rm7kenTV2G" +
          "TqD9qxm035xLXGFjOcR6ttJX/QWWebqcvobBNBngf1znb40JdpB/mMvj47GXfoW7WLcYuuy7tN1UDXqT2myqPrw158Ja7F95vaj74Gvz" +
          "P+0Vf8AJ/Rl/sgeY6+Pod4DvomHzYVPdg3H/mmL/Exxhv6mh1nngj23idM9UbnDyfNcB+Q9xT5RDyXnTYVA50jzph6RlvlrKkd6M10jj" +
          "xDb/7z5AXx/P6CqXHEMe5FUwPRWfgS8yM621xmHkRnsyumTqKz4lVTW9HZ7pqpC+icep15GJ2n/mQ+ROf/bphqic5lt+gXfZtuM9+i77" +
          "c7pjz0LbvL9+hb9ZepOOgbeY98pj1231Q24vfvA1N10Xnkoams6Nz5t6lU6Hz6yFRNdJr/MN7R2eKJKZY8vXlqqlFQLzwz1SYI9AtT7d" +
          "D3jrYJ+j5R7LRA35mX+EY8s7zGN3TmfmNqATpLvyUP0Vnlnamd6GzynrxC54APZriPO/WR+QR9tz6ZKhsrWJ9MxUVnFha5X9BZ4wt5TT" +
          "w7fWWeCvY9svQKvZcNS0XRWZ7NWBDHIbYV5ulkx9IndM50gUbfEs8K1/XYkSwNQF+KyJbYmmpiFEssidpGMZ6SSy9Hs1QMfZej0zrB+m" +
          "ZpGvpyx7K0EH0jYlt6gb5XcSw1RFfJeJbWo+tdfEtN0ZMmoaUp6NmeyFIq9ET+zlLP8DzH0nH0LElqKQY6TiSzlAsd+VNYWoqOA3SeGR" +
          "0rvrfCeF1PZakUOvKn5nd0ZEpjaRbxmpnWkoGOReksRUPHhvRWOO76ZLR0KqgLMlliClCPzJaOoUdZrfBcJ+GPljqgowNF/H50PMpuqS" +
          "DxeZjDUn50NMplaRM6zDyWaqGjVl5Lq9AxLJ+ly+TfsfyWkqDDZ9JqgI5uhSwdCc47CluKj450RfE1qLOKWTLRkbeEpfHo6FfS0hl0HC" +
          "plKSH8ycpY4fnNorKW3qEjWXlLVcHsV8HSWvg/VLSEFA2ojK8I61DF0ny4t1e19ATuH6tbag53hhrf/G9Q09IkuC/WsuTBfau2pVhwP2" +
          "Ly+Q7uDXUt3aN9U89SWvjbNLA0F/53DS2lJw5vSOq08Ldogr/B+Glq6Tzj50QzS/+hoyBFX2f4Z7YkfsThf62scLwUbWOpO/zr2lq6Sx" +
          "wKt+cz/K068Hz8r9bR0igrWM8s1cX/VV0s3XaDcxq4yJ9h3S1FoEM9yQP4f+tl6ULUYP2yVBP+6H0tlYA7Zj/yIKiT+lvKE9QZAyx1Df" +
          "Z1Ay0dDOrIQeiAf+YQSyfhLznMUl+4e1HsrIZ76khLR/E982hLzeBNNNZi7EixxlkaAu+u8VZYf5+cYIX7lJMTaeE9O8lSYXgPT7aUE9" +
          "+XTyHOcB+faikv3Ien8T3+n55uhedlh2fyGf93UZxkgHvlbOIN98E5lrLDXf1XSwfgrjqP/IN7/W+WksH9bD75BHf1hfyO7wkWWxpJ3t" +
          "RcYukQ/O2XWrqO742XkUfoaLycFv/nr2A843vVVZZ+Q0/yNVZ4zpRgnRXupyM2MC7gdjZbqgbvrC2W7sNrbbPC87DBEYwXeKPssFQH3m" +
          "K7LE2EM9UeS13gS7rPUjv4yuy3NB2+z79bKouvpQ5amgrf4EPcD1+iI5ZawVXzqKVFcM1j0P2Dz5+P81y4xpy0RAmvjKfxHZ8nnOF3+D" +
          "6eZZzhb8fzlk7A+eKCpYrwJb1kqR9+Hr9sKQ18B69aSgFf0ut8j5/eDfThY9OblvbCee8W8x+c6e/gG5z1/7K0Hc6I+5bi4tvyh/gA14" +
          "NHVngut/4fS2y5tfappQLw/P7c0k/E++i/5B0cUV5bmo1fj99Y6g3Hs3eW+sNR/SN5Dkf9z5Ye41Pj/yw9o/8XXy0NpN/1hq1C9LvDtF" +
          "UGf9batgrQf1XX1m386ezZ+g+Otr6t9/hzN5KtavgzNIqtV/BUjWqLJVn5otkipGoY3dYZfNrFopoJn6bEtGXClzS2rYnkYeY4tn6Ds3" +
          "9cW08D3+LzPXEdl8AOz9W3JrSVBt6Oie1wPz/uO76He3USW0ng/juprTqM/zzJba3Av7YpbF1Hx/CUdnguOOF7W1/Q83cqO9wfRqS2lQ" +
          "4d6dPZmgP/rPR2OO/+SfFWFv4XGW1RMqt+Zjucd7tmoV8u2Jf1G/+8bLYiw30zu63ycH/OYasN3LFy2xoEd6U8tnbCbeSz1Qnu1gzGi3" +
          "BHK2irH/FeWMhWdHhfF7bVBN5RReEMeCmu/oXzfQlbLeB8X8oOzy8/l+H5cBYqh5/EPV8FW1PhHFfRFiWAZlXmPvgaVrG1Db5E1Wx1g6" +
          "9EdVu/wvdfDVu1iP9/NWnhq1Db1hL4htexdYP56XFdW8XhK1Pf1gJ83dHAVmx8bdiI58H5Z2NbOeF72dQOz/dyMSgnwjeqla078O2giI" +
          "gN19p2tj7h45MOtlhyFIVNZR3ysRyb2pnEO1Z3W43gWdqTPIFnTG+bNZrx0ddWfPo/3I/n0H+8Abaa4dPggbZOwTF8sK1z+LR6qM2ek3" +
          "wZjo/kZf8RxAeO0z/bSgzH7VHEB59ajrG1CZ5942yxxVXqiXa4r347yVYx/Ok7xdZheBJNs9USnobT7fAct8JMW7PwJ2KWLQtf8sy2NZ" +
          "z4PZljh/P1vbm2ssK1Z54dntv+Pt9WNHxZvZBxANeTRVwH1+3FtjLCdXKJrQSMm2lLGTe0nZfZ4f627nJbSxk/9VfYWg5vGTZL0+CtsM" +
          "rWOvwrt9rWGuJbfQ3+w99/LfmHn63X2TqBju4sppfxdfMGfAvm843f8rH/Jq6jPbrZVgb0bN7C7/j8ZKutyuiKFGGrA37H2m6rR6Bvhx" +
          "2eZxTbST7g//JdPAd9o3bb+od23h46Q+ftveQH/h/ex7hB78r9tqKid+HvjBvyYe6Bb+Pr5kFbRdG9/hBxRO/5w+QNet8eIQ+Ceeso4w" +
          "u9WY+Tv+iNhZjm6H1Amwe9B0/aiofeladsUTJq42niAMJCJo/XtOPO2rqJ7obnbK0njjnO2/oZ/fn+sDUO3Wkv2OpLPFNetNUd/S9oi6" +
          "H/4CWei/6Bl22dpS1yxdYUfGh81dZGfCh0zdYkfIh23VZjfHhPW454Z7tB/PEh4ua3eD9jEiiCDy9u81x8uHTHVlp8WH/XloH+o3/ZSk" +
          "T8I+5xPT6svI+OYL55wGd8+P2hrTj4sPJvvqdd/Yh5FD+mPbb1AD96spicxY8iT2zNxo/mT22x9VFrNiEsWRr+HP34MOmFrcf40JdBfw" +
          "EffnpJ/uJDkVfchw/FMGsuPsR6Y6s9PqR9azM348s7W/3Rn+q9rYGB/g+2dqA/80dbo9Cf9ZOtMejP+Blf0V/pf3b4niLjf3xmHGb9wu" +
          "/4UOkr3+NDXTnajA8DDUdX8GGt6cgL9FuOYqG7jONoIXkw0OV39MfwHbVFb8NIjiLQO5dN5C30Lozi6D56p0R1xFZKkaI7YmrT8hiO/k" +
          "HnfzGd8L3YtNiOLqMvV1xHfdDVPJ6jZeialcDRdfRsTOToDToyJnHUCe62SR2tgXtCMic8nzuf3FFceL+mcML3eNO+53nE7WUqRznhrZ" +
          "na0S/wDk/j6Ai8+9I6MuGNk95RLXgHZnC0Fd7XGR1lhLNvZkcbicuVLI5AVawfHZUL5t/sjqrCmTGHE9a3OXI6YRzW53L0N7yTcjs6Sh" +
          "yMvI7y4X+5fI6Gwr2WYu0B/qcv6Kgp/DEKOyoD9+wijs7jd182MRvh/ruEo+Twni/tKBJ5lL4c18N3toKj5/j4sZIjlkw9qOKEdVPV6o" +
          "66wPF7TUd36L8xk3JwnjmPYnY3/U6q52gd/c5rwGf6PdkQ3/FtUmO+p9+NTfAvqJuaOkqMX+mbOypLnPe0cMJz89atHI3Er5RtiBN+DW" +
          "/raBE8H9lURjGC9cAJzze3dkI3XIm64B8+xermKBU+Ve/uhPPw7B5O+L4hVS9HufAnTh9HqeGc0tfRYjjv9XP0Fs6I/o6Ok58RA5xwHx" +
          "YxkBbeoYMdTYP37yGOPsLbepgT1s/thzsaCO+4EU74/qHrSEfDg3lpFPkKb0GVjVlmrKPKxLbUOHIP5rrjnfBsZ8oEJzyj6z0Jz4jxjs" +
          "mO9sN+YQo5h7c9pzkaAHvWGY5y4+3LmY4+wP7nL47+CuaWOY5KwlzsVzwL1v55jlbA+v43R1+IbdcF5DKssRYRM1hjLHYUH9a2S4gZrG" +
          "OWOZoMa8sVjjrAem+VE84R69c44d5kyjq0hTUyHsH18yZHXfH02mZHh+H6aaujAnDt2eZoJVzrt3MfXM13OvoJrly7HaWEq/4ecg2u6n" +
          "sdFYFr/j5H4/AywQFHFlwnDzJm4fp8iBwOuI446glX66OOqpODV445OghflBOO3hP7aCcdPYHzPe0l/LROO7oLb5wzjl7iZ46zzBH4Wf" +
          "Wco6xwlzvvKC1+lvgDHvibX3RUjFyYdgk9YQ3thGfPX684uslY736NMUZOpP3TkUMujLvB3IKOgywQC9FR5LajpPA/uANXMObvch9jJ/" +
          "29b3NUkfv8jo54Dxy9pv1Ie4Wxf/ghPqLr+N/0h660TLQOehYyEXVHT/unjgqhY/EzJ9juaetzR5QdevzC0S709H7JHICOHa+ccE06/M" +
          "bRLHREe+fodrAWvXfCs/MHH/ET/q2fuR/+9l94Lr7flKv18EaxXN3E9/mOq65w9fddVcLf5ZHd8Cy8bjRXcem/UAxXTOOaG9MN9yAJYr" +
          "v6g/47x3GVgfjXjct19J88vqvr9F8ugSub/s8ndDWePPg9sasBwZyfxNVDOOYndcN3Gr2Tu+FZ8ekUrkbDE5HSVTf8m/e9G75zaZrKVR" +
          "J8zPGDq+e0rVO7+h7On9K4iox/RdK6+kSbIB08+Jg6vas7wZ41g6uL+HmbdiY6zjIRjCQ/5lFY18DX05nd8Gx/fhb0oGswyZE7qCF/RB" +
          "z/UmZzdTXQmd3VmWBPmMPVbnz+SrsNvTdzupqO3o25XLVB7+zcbrhH7J4HP9A9I6+riuiekM9VyWBvm99VE/S/LuBqFfoTFXJ1jjzKzI" +
          "b5Pj6UK0Jn+PC1KM/Hh3nF4UR/pZL4SN7MLe2qWrBWlHXDd3OJKri6FuwlKrmai47qP7mKA/+TKq5W86jU1Vw9gHtudVf14bVqujoI78" +
          "8ErxScj+u4Wgnfi7rkAXxN67tKCVfGhq6ewFW7sasE8Jxv4moKPPGau7oEz6yWrmrj/4XWrqbhd7a2rl4GtWU7V14Kxm17V7PwuURHV7" +
          "eDmrKTG75rz9eF++Hb09VVXbjad+d6uGb0cJUNrrq9XL3Fx6O9XTXHv3194Q7Wqv5uWCPNHegqDz59HsTzyd/MQ10dD/auw9ywJr40nP" +
          "wlPzb+TP7C132UqxjBmjXG1WH41o91w7/1OT7ODc/U801wtTeo/SYSd/iMyeQHfB2nuHqFf0enEj/inXK6q2VwVp3h6jKcTWeSv8H8O4" +
          "s4wPn2F1f9ghp3jhuucX3nuvpCnCPNczURH2/SNoM7+XxXS+BOv8AN17oCC93wbHn9Irjw9R6Tahv4Cy11dYB4T1nmKn6Q3ytcHULHd6" +
          "tcLSCfvyO4Y9HRfA35jo6ea91wbzZlnRv+UcbfFKy10FFso6sN4Tzt6g1+D9+MLvSc3uKqIDoitrpKE+zV2cg1IB+uRbgqi57NO9zwHU" +
          "/zXTwfHfd2k3exg/karmCPtI/noGM9i1My+CcdQC/+xzjkalCwJzmMv/jf+yg+wX/tmBvW4odPuMoF97WTxBPu9KddzYb79RniDXeU86" +
          "4ItUr94WorvHUvEl98H3WZ+MJ58wpxhS/zdVfz4Mt8gzZYm2+6SgtXpNuu+sL1+Y6r9nA9/os8w9cy991wPUn5kDyGy3rkqjt+nn/sqh" +
          "h+lnrqakZQAz93tZlxdPylq0T0v+eNq2j4VPu9G64Hcz8xLug3z3+uRuFHza+ultJfbcPTcvq7YHlKRT/7HE8x6Cep56kj+uP5nlrSX/" +
          "JInjonC/6exNNEfLgU1VMG9KeM7qkX/daN6YklViVie/qF/hfG8fSOeM1l4/VvML8k9MJ3/CUS8zscF5J44buLCsk8LUD/4+SeCpNXcV" +
          "J6agNPy+897SQu+1J5igtX79ReePYUi8UvOHv6Lp2nLnCNyeDpFj54mTw1xYdKWdADT7FsnkhlvczhqQJxGJfb0wM44uVDB/meoICnrs" +
          "F8XtgLa/amFM676D9pcU+9g/WvhKfh+B+tlKfW9Ju5tKdR+HK7jKd89Hu0vBeehayu5OkRPtSs4mkq+o9X8+Sie0pNT2fwf35tL/ybi3" +
          "FMGsfod3g9T/vRbzXwwr/dKtLQ0wB86N/I0/bgbKmxp0xw/NzU0wF8yNrcUwc4drX09AGOa628cFylbOOpERyV2noaG9Q77b7xnG2P38" +
          "H629HTeLhedvIUmx86diFu8HXu6ompQvO7eeGZeM0exA9/yvXy1A5/LvTxwneljft5GhLM+/09ncCnSQO98O86tw72dJm8+TjUU2J8qj" +
          "3cUx/4lo7wdIQ4HWSjeR/OzyM9JYHTGOPpe/gyjvVUkTj1Hoff8K0dj9/E66eJnhoSr9mTiRt8H6d68vBv9XRPO4I9wUwv3JueneXpJj" +
          "4mmuMpLVzZ5noqAte+Xz2dxseFv3laj3/7FvAZ/xYv8sI9wIwlnhbDNWMZLfGLWOHpcHCWusrTWnjervHCs7hJ6/ALjj83eLoKx4RN+B" +
          "qcxWzxFD2Y5xnUU4nnhQhP5+BouMNTvaA+3OWF62LKvZ6S0f+TfeQD/iw+QF7Tf+pD6Kf/97TBO+P0R7zwb1X2HCW/8afpceKIPwVOMt" +
          "7w5egpL3w3U4lFezA8087BC091ipVgD7/vgqeXfrBn95QSnveXPaWHZ9JVT78Ttz+veYqGPyn/9FQdrro3PI3Dn9S3PNWGb/VtfISv/l" +
          "1PE8ivKfc8HSR+FottLuKW/qGn+vAV+9tTd/hGPSIO+PXyMfMDfuV4Ai+ceZ55ahbUqS8Yt+RXvH+98G8b47z0wjP0fK+88N1YsdfkL7" +
          "ypmcSr4GPPt174bmz4e+aLoM7+6Ok6Pt797CkqnAW+ML7hHPfVC8/UUhq+KsI5xvK1Bc6Bth/+zU8MBll+OOeTLCfgfO37ShbUFZF9lY" +
          "SzUFRfdfGxaTRfA+GMFcNXZjhLxfTF9kzzY/liiVDLOL6Gwjc8nq/5cOVL4KsmXNES++HfbnVN4msCXDOS+tpI/l9L5ustfLlS+KpKns" +
          "2mCN8KX6QffKUO6uXUvurjY+00vrrDVy6tr5bwtU3n62d8PJje120452X0w/onUmZfpeBLn4XriHfWbL7aB2en2fkd/zbm8PUMvmi5fZ" +
          "VjHOzK4+t9MA7ywxms1wXpH767hXwlhatzEV+r8a1xUV9z4ZtQzA/rjK/FfRXAP6+krxJwpi3tqzl81cv4mgRfmbK+hsNXvRyf4Wta3t" +
          "c8fLxSAV/xsXYlP/zb4pOV/XA+GVjFF1scrWQD/y+8xar7Gg3nsxp++I4kQW1fjeCcUsfXVfIyoq6vL8Q5Rn1ftcjLHA189YN7eUNfz/" +
          "E1dWNfHeC+1NRXkuCMsLmv7HAubenrKX7ebe0rXTBu2/p6CN/Ndn4433UmuWbCF9HR1y34Onb2NZ0fnnXxRUmv+t18DcPHSD18/Uj+le" +
          "jpqx18vXuhBz8H9va1BM57fXxFhm9Ufz/c498e4MuF6/ZA2mCfNNhXq2AfOsTXL/g6iaQJ6twXw3zFx9fVI3z9Ae+EkeQpvu4Y5Ydnvm" +
          "XG+OoE7/ux33g/s9nx4Z07gTyG9/ZE9AXzziRfj4Kz/8m+2CLq9RTMDPZ503xhtbpO9zUI/voz/bBeXzmL/uGOMRu/4D48h/7Jh1i/Ei" +
          "98PU97K1ZQv/nhGWX3+b6GwO8sJH5wJ13kK0Pg82JfH+DPsdQPzyTfLvNlwt1+ha8++N17pa8R8C9d7esM/LPX+uE7i2fr0Q3nyY2+KD" +
          "W0Y7Ovv/xgH+2HZ06tt/maHNQ9EfhH/Fvv4DOcR3eSL3D+HwCCjIRqMAAA");

        RecordInputStream in = TestcaseRecordInputStream.create(bytes);
        assertEquals(ExtSSTRecord.sid, in.getSid());
        ExtSSTRecord src = new ExtSSTRecord(in);
        assertEquals(12386, src.getDataSize());
        ExtSSTRecord.InfoSubRecord[] sub1 = src.getInfoSubRecords();

        byte[] serialized = src.serialize();

        in = TestcaseRecordInputStream.create(serialized);
        assertEquals(ExtSSTRecord.sid, in.getSid());
        ExtSSTRecord dst = new ExtSSTRecord(in);
        assertEquals(12386, dst.getDataSize());
        ExtSSTRecord.InfoSubRecord[] sub2 = src.getInfoSubRecords();
        assertEquals(sub1.length, sub2.length);

        for(int i = 0; i < sub1.length; i++){
            ExtSSTRecord.InfoSubRecord s1 = sub1[i];
            ExtSSTRecord.InfoSubRecord s2 = sub2[i];

            assertEquals(s1.getBucketSSTOffset(), s2.getBucketSSTOffset());
            assertEquals(s1.getStreamPos(), s2.getStreamPos());

        }
    }


}
